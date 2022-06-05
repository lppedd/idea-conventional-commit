package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.ConfigurationChangedListener
import com.github.lppedd.cc.document
import com.github.lppedd.cc.language.ConventionalCommitFileType
import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.lang.Language
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.ui.EditChangelistSupport
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.EditorTextField
import com.intellij.ui.LanguageTextField
import com.intellij.util.Consumer
import com.intellij.util.LocalTimeCounter
import java.util.*
import java.util.function.Supplier
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitEditChangelistSupport(project: Project) : EditChangelistSupport {
  private val textFields = Collections.newSetFromMap<LanguageTextField>(WeakHashMap(4))

  init {
    val connection = project.messageBus.connect(project)
    connection.subscribe(ConfigurationChangedListener.TOPIC, ConfigurationChangedListener(::updateTextFields))
  }

  override fun installSearch(name: EditorTextField, comment: EditorTextField) {
    if (comment is LanguageTextField) {
      hackLanguageTextField(comment)
    }
  }

  override fun addControls(bottomPanel: JPanel, initial: LocalChangeList?): Consumer<LocalChangeList>? =
    null

  override fun changelistCreated(changeList: LocalChangeList) {}

  private fun updateTextFields() {
    textFields.forEach {
      val editor = it.editor

      if (editor != null && !editor.isDisposed) {
        val configService = it.project.service<CCConfigService>()

        if (configService.isEnableLanguageSupport) {
          installConventionalCommitLanguage(it, it.project)
        } else {
          installPlainTextLanguage(it, it.project)
        }
      }
    }
  }

  private fun hackLanguageTextField(comment: LanguageTextField) {
    textFields.add(comment)
    val project = comment.project

    if (!project.service<CCConfigService>().isEnableLanguageSupport) {
      return
    }

    installConventionalCommitLanguage(comment, project)
  }

  private fun installConventionalCommitLanguage(textField: LanguageTextField, project: Project) {
    val psiFileFactory = project.service<PsiFileFactory>()
    val oldDocument = textField.document
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

    getChangesSupplierKey()?.also {
      val changesSupplier = oldDocument.getUserData(it)
      document.putUserData(it, changesSupplier)
    }

    document.addDocumentListener(object : DocumentListener {
      override fun documentChanged(event: DocumentEvent) {
        val editor = textField.editor ?: return
        editor.contentComponent.repaint()
      }
    })

    textField.setLanguage(ConventionalCommitLanguage)
    textField.setNewDocumentAndFileType(ConventionalCommitFileType, document)
  }

  private fun installPlainTextLanguage(textField: LanguageTextField, project: Project) {
    val psiFileFactory = project.service<PsiFileFactory>()
    val oldDocument = textField.document
    val psiFile = psiFileFactory.createFileFromText(
        "Dummy." + PlainTextFileType.INSTANCE.defaultExtension,
        PlainTextFileType.INSTANCE,
        oldDocument.charsSequence,
        LocalTimeCounter.currentTime(),
        true,
        false,
    )

    val document = psiFile.document ?: return
    val commitMessage = oldDocument.getUserData(CommitMessage.DATA_KEY)
    document.putUserData(CommitMessage.DATA_KEY, commitMessage)

    getChangesSupplierKey()?.also {
      val changesSupplier = oldDocument.getUserData(it)
      document.putUserData(it, changesSupplier)
    }

    textField.setLanguage(PlainTextLanguage.INSTANCE)
    textField.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, document)
  }

  private fun LanguageTextField.setLanguage(language: Language) {
    val languageField = LanguageTextField::class.java.getDeclaredField("myLanguage")
    languageField.trySetAccessible()
    languageField.set(this, language)
  }

  @Suppress("unchecked_cast")
  @Compatibility(minVersion = "213.3714.440", replaceWith = "CommitMessage.CHANGES_SUPPLIER_KEY")
  private fun getChangesSupplierKey(): Key<Supplier<Iterable<Change>>>? =
    try {
      val field = CommitMessage::class.java.getDeclaredField("CHANGES_SUPPLIER_KEY")
      field.trySetAccessible()
      field.get(null) as? Key<Supplier<Iterable<Change>>>
    } catch (e: NoSuchFieldException) {
      null
    }
}
