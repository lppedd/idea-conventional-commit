package com.github.lppedd.cc.editor

import com.github.lppedd.cc.CCEditorUtils
import com.github.lppedd.cc.CCParser
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

/**
 * @author Edoardo Luppi
 */
internal class CommitTypedHandlerDelegate : TypedHandlerDelegate() {
  override fun beforeCharTyped(
    ch: Char,
    project: Project,
    editor: Editor,
    file: PsiFile,
    fileType: FileType
  ): Result {
    if (!isCommitDialogContext(editor)) {
      return CONTINUE
    }

    val lineUntilCaret = CCEditorUtils.getCurrentLineUntilCaret(editor)
    val (type, _, _, separator) = CCParser.parseText(lineUntilCaret)

    if (!type.isValid || separator) {
      return CONTINUE
    }

    return when (ch) {
      ':'  -> {
        EditorModificationUtil.insertStringAtCaret(editor, ": ")
        STOP
      }
      '('  -> {
        EditorModificationUtil.insertStringAtCaret(editor, "()")
        EditorModificationUtil.moveCaretRelatively(editor, -1)
        STOP
      }
      else -> CONTINUE
    }
  }

  override fun checkAutoPopup(
    ch: Char,
    project: Project,
    editor: Editor,
    file: PsiFile
  ): Result {
    if (!isCommitDialogContext(editor)) {
      return CONTINUE
    }

    val lineUntilCaret = CCEditorUtils.getCurrentLineUntilCaret(editor)
    val (type, _, _, separator) = CCParser.parseText(lineUntilCaret)

    if (!type.isValid || separator) {
      return CONTINUE
    }

    return if (ch == ':' || ch == '(') {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      STOP
    } else {
      CONTINUE
    }
  }

  private fun isCommitDialogContext(editor: Editor) =
    editor.document.getUserData(CommitMessage.DATA_KEY) != null
}
