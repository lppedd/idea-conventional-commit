package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.document
import com.github.lppedd.cc.language.ConventionalCommitFileType
import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
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
import java.util.function.Supplier
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitEditChangelistSupport : EditChangelistSupport {
  override fun installSearch(name: EditorTextField, comment: EditorTextField) {
    val project = comment.project
    val psiFileFactory = project.service<PsiFileFactory>()
    val oldDocument = comment.document
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

    comment.setNewDocumentAndFileType(ConventionalCommitFileType, document)

    val languageField = LanguageTextField::class.java.getDeclaredField("myLanguage")
    languageField.trySetAccessible()
    languageField.set(comment, ConventionalCommitLanguage)

    document.addDocumentListener(object : DocumentListener {
      override fun documentChanged(event: DocumentEvent) {
        val editor = comment.editor ?: return
        editor.contentComponent.repaint()
      }
    })
  }

  override fun addControls(bottomPanel: JPanel, initial: LocalChangeList?): Consumer<LocalChangeList>? =
    null

  override fun changelistCreated(changeList: LocalChangeList) {}

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
