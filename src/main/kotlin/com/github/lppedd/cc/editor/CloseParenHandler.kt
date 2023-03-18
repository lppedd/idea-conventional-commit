package com.github.lppedd.cc.editor

import com.github.lppedd.cc.moveCaretRelatively
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CloseParenHandler : BaseTypedHandler(')') {
  override fun beforeCharTyped(commitTokens: CommitTokens, project: Project, editor: Editor): Result {
    if (commitTokens.scope !is ValidToken) {
      return CONTINUE
    }

    val caretModel = editor.caretModel
    val lineOffset = caretModel.logicalPosition.column
    val textOffset = caretModel.offset
    val text = editor.document.immutableCharSequence

    return if (
        lineOffset == commitTokens.scope.range.endOffset &&
        textOffset < text.length && text[textOffset] == myChar) {
      // type(...|)
      editor.moveCaretRelatively(1)
      STOP
    } else {
      CONTINUE
    }
  }

  override fun checkAutoPopup(commitTokens: CommitTokens, project: Project, editor: Editor): Result =
    CONTINUE
}
