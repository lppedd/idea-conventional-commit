package com.github.lppedd.cc.editor

import com.github.lppedd.cc.insertStringAtCaret
import com.github.lppedd.cc.moveCaretRelatively
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.Separator
import com.github.lppedd.cc.parser.Subject
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.scheduleAutoPopup
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP
import com.intellij.openapi.editor.Editor

/**
 * @author Edoardo Luppi
 */
private class ColonHandler : BaseTypedHandler(':') {
  override fun beforeCharTyped(commitTokens: CommitTokens, editor: Editor): Result {
    val type = commitTokens.type as ValidToken
    val scope = commitTokens.scope
    val lineOffset = editor.caretModel.logicalPosition.column

    if (scope !is ValidToken && type.range.last == lineOffset ||
        scope is ValidToken && scope.range.last == lineOffset - 1) {
      doInsertColon(commitTokens.separator, commitTokens.subject, editor)
      return STOP
    }

    return CONTINUE
  }

  override fun checkAutoPopup(commitTokens: CommitTokens, editor: Editor): Result {
    val lineOffset = editor.caretModel.logicalPosition.column
    return if (
        (commitTokens.type as ValidToken).range.last == lineOffset ||
        commitTokens.scope is ValidToken && commitTokens.scope.range.last == lineOffset - 1
    ) {
      editor.scheduleAutoPopup()
      STOP
    } else {
      CONTINUE
    }
  }

  // type|
  // type|:
  // type|: my desc
  // type(scope)|
  // type(scope)|:
  // type(scope)|: my desc
  private fun doInsertColon(separator: Separator, subject: Subject, editor: Editor) {
    if (separator.isPresent) {
      editor.moveCaretRelatively(1)
    } else {
      editor.insertStringAtCaret(":")
    }

    if (subject is ValidToken) {
      // type:| my desc
      val caretShift = if (subject.value.startsWith(' ')) 1 else 0
      editor.moveCaretRelatively(caretShift)
    } else {
      // type:|
      editor.insertStringAtCaret(" ")
    }
  }
}
