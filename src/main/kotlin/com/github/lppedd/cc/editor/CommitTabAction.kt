package com.github.lppedd.cc.editor

import com.github.lppedd.cc.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actions.TabAction
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.ui.CommitMessage

private val MOVE_CARET_KEY = Key.create<Unit>("Vcs.CommitMessage.moveCaret")

/**
 * @author Edoardo Luppi
 */
private class CommitTabAction : TabAction() {
  init {
    setupHandler(CommitTabHandler)
  }

  object CommitTabHandler : Handler() {
    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
      val document = editor.document

      if (document.getUserData(MOVE_CARET_KEY) != null) {
        document.putUserData(MOVE_CARET_KEY, null)
        editor.moveCaretRelatively(1)
        editor.scheduleAutoPopup()
      } else {
        super.executeWriteAction(editor, caret, dataContext)
      }
    }

    override fun isEnabled(editor: Editor, dataContext: DataContext): Boolean {
      val document = editor.document

      if (document.getUserData(CommitMessage.DATA_KEY) != null) {
        val (lineStart, lineEnd) = editor.getCurrentLineRange()
        val lineText = document.getSegment(lineStart until lineEnd)
        val lineCaretOffset = editor.caretModel.offset - lineStart
        val scope = CCParser.parseHeader(lineText).scope

        if (scope is ValidToken && (
                lineCaretOffset == scope.range.first - 1 ||
                lineCaretOffset == scope.range.last)) {
          document.putUserData(MOVE_CARET_KEY, Unit)
          return true
        }
      }

      @Suppress("DEPRECATION")
      return super.isEnabled(editor, dataContext)
    }
  }
}
