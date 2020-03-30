package com.github.lppedd.cc.editor

import com.github.lppedd.cc.moveCaretRelatively
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP
import com.intellij.openapi.editor.Editor

/**
 * @author Edoardo Luppi
 */
private class CloseParenHandler : BaseTypedHandler(')') {
  override fun beforeCharTyped(commitTokens: CommitTokens, editor: Editor): Result {
    val caretOffset = editor.caretModel.logicalPosition.column
    val text = editor.document.immutableCharSequence
    return if (
        caretOffset == (commitTokens.scope as ValidToken).range.last &&
        caretOffset < text.length && text[caretOffset] == myChar) {
      // type(...|)
      editor.moveCaretRelatively(1)
      STOP
    } else {
      CONTINUE
    }
  }

  override fun checkAutoPopup(commitTokens: CommitTokens, editor: Editor): Result = CONTINUE
}
