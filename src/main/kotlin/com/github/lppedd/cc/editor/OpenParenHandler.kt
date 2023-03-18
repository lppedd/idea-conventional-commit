package com.github.lppedd.cc.editor

import com.github.lppedd.cc.insertStringAtCaret
import com.github.lppedd.cc.moveCaretRelatively
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.scheduleAutoPopup
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class OpenParenHandler : BaseTypedHandler('(') {
  override fun beforeCharTyped(commitTokens: CommitTokens, project: Project, editor: Editor): Result {
    val type = commitTokens.type as ValidToken
    val lineOffset = editor.caretModel.logicalPosition.column

    if (type.range.endOffset != lineOffset) {
      return CONTINUE
    }

    val scope = commitTokens.scope

    if (scope is ValidToken) {
      handleTypeWithScope(editor, scope)
    } else {
      handleTypeOnly(editor)
    }

    return STOP
  }

  override fun checkAutoPopup(commitTokens: CommitTokens, project: Project, editor: Editor): Result {
    val lineOffset = editor.caretModel.logicalPosition.column
    return if ((commitTokens.type as ValidToken).range.endOffset == lineOffset) {
      editor.scheduleAutoPopup()
      STOP
    } else {
      CONTINUE
    }
  }

  // type|(scope...
  private fun handleTypeWithScope(editor: Editor, scope: ValidToken) {
    val newOffset = editor.moveCaretRelatively(1)

    if (scope.range.isEmpty) {
      val text = editor.document.immutableCharSequence

      if (text.length > newOffset && ')' != text[newOffset] || text.length == newOffset) {
        // type(|
        editor.insertStringAtCaret(")")
        editor.moveCaretRelatively(-1)
      }
    }
  }

  // type|
  private fun handleTypeOnly(editor: Editor) {
    editor.insertStringAtCaret("()")
    editor.moveCaretRelatively(-1)
  }
}
