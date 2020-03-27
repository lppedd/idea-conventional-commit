package com.github.lppedd.cc.editor

import com.github.lppedd.cc.getCurrentLineUntilCaret
import com.github.lppedd.cc.insertStringAtCaret
import com.github.lppedd.cc.moveCaretRelatively
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.scheduleAutoPopup
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiFile

/**
 * @author Edoardo Luppi
 */
private class CommitTypedHandlerDelegate : TypedHandlerDelegate() {
  override fun beforeCharTyped(
      ch: Char,
      project: Project,
      editor: Editor,
      file: PsiFile,
      fileType: FileType,
  ): Result {
    if (!isInValidContext(editor)) {
      return CONTINUE
    }

    return when (ch) {
      ':' -> handleColon(editor)
      '(' -> handleOpeningParen(editor)
      ')' -> handleClosingParen(editor)
      else -> CONTINUE
    }
  }

  override fun checkAutoPopup(
      ch: Char,
      project: Project,
      editor: Editor,
      file: PsiFile,
  ): Result {
    if (!isInValidContext(editor)) {
      return CONTINUE
    }

    return if (':' == ch || '(' == ch) {
      editor.scheduleAutoPopup()
      STOP
    } else {
      CONTINUE
    }
  }

  private fun handleColon(editor: Editor): Result {
    val text = editor.document.charsSequence
    val caretModel = editor.caretModel
    val caretOffset = caretModel.offset

    if (':' == text.getOrNull(caretOffset)) {
      val caretShift = if (' ' == text.getOrNull(caretOffset + 1)) 2 else 1
      editor.moveCaretRelatively(caretShift)
    } else {
      editor.insertStringAtCaret(": ")
    }

    return STOP
  }

  private fun handleOpeningParen(editor: Editor): Result {
    val text = editor.document.charsSequence
    val caretModel = editor.caretModel

    if ('(' == text.getOrNull(caretModel.offset)) {
      editor.moveCaretRelatively(1)

      if (' ' == text.getOrElse(caretModel.offset) { ' ' }) {
        editor.insertStringAtCaret(")")
        editor.moveCaretRelatively(-1)
      }
    } else {
      editor.insertStringAtCaret("()")
      editor.moveCaretRelatively(-1)
    }

    return STOP
  }

  private fun handleClosingParen(editor: Editor): Result =
    if (')' == editor.document.charsSequence.getOrNull(editor.caretModel.offset)) {
      editor.moveCaretRelatively(1)
      STOP
    } else {
      CONTINUE
    }

  private fun isInValidContext(editor: Editor): Boolean {
    if (editor.document.getUserData(CommitMessage.DATA_KEY) == null) {
      return false
    }

    val lineUntilCaret = editor.getCurrentLineUntilCaret()
    val (type, _, _, separator) = CCParser.parseHeader(lineUntilCaret)
    return type is ValidToken && !separator.isPresent
  }
}
