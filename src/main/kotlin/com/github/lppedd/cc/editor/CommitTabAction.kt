package com.github.lppedd.cc.editor

import com.github.lppedd.cc.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actions.TabAction
import com.intellij.openapi.util.Key

/**
 * @author Edoardo Luppi
 */
private class CommitTabAction : TabAction() {
  init {
    setupHandler(CommitTabHandler)
  }

  private object CommitTabHandler : Handler() {
    private val moveCaretKey = Key.create<Unit>("Vcs.CommitMessage.moveCaret")

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
      val document = editor.document

      if (document.getUserData(moveCaretKey) != null) {
        document.putUserData(moveCaretKey, null)
        editor.moveCaretRelatively(1)
        editor.scheduleAutoPopup()
      } else {
        super.executeWriteAction(editor, caret, dataContext)
      }
    }

    override fun isEnabled(editor: Editor, dataContext: DataContext): Boolean {
      val document = editor.document

      if (document.isCommitMessage()) {
        val (lineStart, lineEnd) = editor.getCurrentLineRange()
        val lineText = document.getSegment(lineStart, lineEnd)
        val lineCaretOffset = editor.caretModel.offset - lineStart
        val scope = CCParser.parseHeader(lineText).scope

        if (scope is ValidToken && (
                lineCaretOffset == scope.range.startOffset - 1 ||
                lineCaretOffset == scope.range.endOffset)) {
          document.putUserData(moveCaretKey, Unit)
          return true
        }
      }

      @Suppress("deprecation")
      return super.isEnabled(editor, dataContext)
    }

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
      if (editor.document.isCommitMessage()) {
        true
      } else {
        super.isEnabledForCaret(editor, caret, dataContext)
      }
  }
}
