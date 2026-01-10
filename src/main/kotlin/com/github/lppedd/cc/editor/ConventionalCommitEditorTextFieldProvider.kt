package com.github.lppedd.cc.editor

import com.github.lppedd.cc.configuration.ConfigurationChangedListener
import com.github.lppedd.cc.document
import com.github.lppedd.cc.isCommitMessage
import com.github.lppedd.cc.language.ConventionalCommitFileType
import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.EditorCustomization
import com.intellij.ui.EditorTextField
import com.intellij.ui.EditorTextFieldProviderImpl
import com.intellij.ui.LanguageTextField
import com.intellij.util.LocalTimeCounter
import java.awt.event.HierarchyEvent
import java.lang.reflect.Field
import java.util.*

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitEditorTextFieldProvider : EditorTextFieldProviderImpl() {
  private val projects = Collections.newSetFromMap<Project>(WeakHashMap(8))
  private val editorFields = Collections.newSetFromMap<LanguageTextField>(WeakHashMap(32))
  private val keysMapField by lazy(::findMyKeysMapField)

  override fun getEditorField(
    language: Language,
    project: Project,
    features: Iterable<EditorCustomization>,
  ): EditorTextField {
    if (!projects.contains(project)) {
      connectProject(project)
      projects.add(project)
    }

    val editorField = super.getEditorField(language, project, features)

    if (editorField is LanguageTextField && language is PlainTextLanguage) {
      editorField.addHierarchyListener {
        if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L && it.changed.isShowing) {
          adaptEditorField(editorField, project)
        }
      }
    }

    return editorField
  }

  private fun connectProject(project: Project) {
    val handler = MyConfigurationChangedListener(project)
    val connection = project.messageBus.connect()
    connection.subscribe(ConfigurationChangedListener.TOPIC, handler)
  }

  private fun adaptEditorField(editorField: LanguageTextField, project: Project) {
    if (!editorFields.contains(editorField)) {
      val editor = editorField.editor

      if (editor != null && !editor.isDisposed && editor.document.isCommitMessage()) {
        editorFields.add(editorField)
        installConventionalCommitLanguage(editorField, project)
      }
    }
  }

  private fun refreshEditor(editor: EditorEx) {
    val highlighter = editor.highlighter
    val field = keysMapField

    if (field != null && highlighter is LexerEditorHighlighter) {
      val keysMap = field.get(highlighter) as MutableMap<*, *>
      keysMap.clear()
    }

    editor.reinitSettings()
  }

  private fun installConventionalCommitLanguage(editorField: LanguageTextField, project: Project) {
    val psiFileFactory = PsiFileFactory.getInstance(project)
    val oldDocument = editorField.document
    val psiFile = psiFileFactory.createFileFromText(
      "Dummy." + ConventionalCommitFileType.defaultExtension,
      ConventionalCommitFileType,
      oldDocument.charsSequence,
      LocalTimeCounter.currentTime(),
      true,
      false,
    )

    val document = psiFile.document ?: return
    val commitMessage = oldDocument.getUserData(CommitMessage.DATA_KEY)
    document.putUserData(CommitMessage.DATA_KEY, commitMessage)
    document.putUserData(CommitMessage.CHANGES_SUPPLIER_KEY, oldDocument.getUserData(CommitMessage.CHANGES_SUPPLIER_KEY))

    editorField.setLanguage(ConventionalCommitLanguage)
    editorField.setNewDocumentAndFileType(ConventionalCommitFileType, document)
  }

  private fun LanguageTextField.setLanguage(language: Language) {
    val languageField = LanguageTextField::class.java.getDeclaredField("myLanguage")
    languageField.trySetAccessible()
    languageField.set(this, language)
  }

  private fun findMyKeysMapField(): Field? {
    for (field in LexerEditorHighlighter::class.java.declaredFields) {
      if (field.name == "myKeysMap") {
        field.trySetAccessible()
        return field
      }
    }

    return null
  }

  private inner class MyConfigurationChangedListener(private val project: Project) : ConfigurationChangedListener {
    override fun onConfigurationChanged() {
      editorFields.asSequence()
        .filter { it.project == project }
        .map(EditorTextField::getEditor)
        .filterIsInstance<EditorEx>()
        .filterNot(EditorEx::isDisposed)
        .forEach(::refreshEditor)
    }
  }
}
