package com.github.lppedd.cc.editor

import com.github.lppedd.cc.ConventionalCommitParser
import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiFile
import kotlin.math.max

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitTypedHandlerDelegate : TypedHandlerDelegate() {
  override fun beforeCharTyped(
    ch: Char,
    project: Project,
    editor: Editor,
    file: PsiFile,
    fileType: FileType
  ): Result {
    val document = editor.document

    if (document.getUserData(CommitMessage.DATA_KEY) == null) {
      return CONTINUE
    }

    val textPrecedingCaret = document.text.run {
      take(max(0, editor.caretModel.offset))
    }

    val (type, _, _, separator) = ConventionalCommitParser.parseText(textPrecedingCaret)

    if (!type.isValid || separator) {
      return CONTINUE
    }

    if (ch == ':') {
      EditorModificationUtil.insertStringAtCaret(editor, ": ")
      return STOP
    }

    if (ch == '(') {
      EditorModificationUtil.insertStringAtCaret(editor, "()")
      EditorModificationUtil.moveCaretRelatively(editor, -1)
      return STOP
    }

    return CONTINUE
  }

  override fun checkAutoPopup(
    ch: Char,
    project: Project,
    editor: Editor,
    file: PsiFile
  ): Result {
    val document = editor.document

    if (document.getUserData(CommitMessage.DATA_KEY) == null) {
      return CONTINUE
    }

    val textPrecedingCaret = document.text.run {
      take(max(0, editor.caretModel.offset))
    }

    val (type, _, _, separator) = ConventionalCommitParser.parseText(textPrecedingCaret)

    if (!type.isValid || separator) {
      return CONTINUE
    }

    if (ch == ':' || ch == '(') {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      return STOP
    }

    return CONTINUE
  }
}
