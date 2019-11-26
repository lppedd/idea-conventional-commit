package com.github.lppedd.cc.action

import com.github.lppedd.cc.CCEditorUtils
import com.github.lppedd.cc.CCParser
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.actions.TabAction.Handler
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.ui.CommitMessage

/**
 * @author Edoardo Luppi
 */
internal class CommitTabHandler : Handler() {
  companion object {
    private val MOVE_CARET = Key.create<Unit>("Vcs.CommitMessage.moveCaret")
  }

  override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
    val document = editor.document

    if (document.getUserData(MOVE_CARET) != null) {
      EditorModificationUtil.moveCaretRelatively(editor, 1)
      document.putUserData(MOVE_CARET, null)
    } else {
      super.executeWriteAction(editor, caret, dataContext)
    }
  }

  override fun isEnabled(editor: Editor, dataContext: DataContext): Boolean {
    val document = editor.document

    if (document.getUserData(CommitMessage.DATA_KEY) != null) {
      val lineRange = CCEditorUtils.getCurrentLineRange(editor)
      val lineText = document.text.substring(lineRange.first, lineRange.last)
      val lineCaretOffset = editor.caretModel.offset - lineRange.first
      val scope = CCParser.parseText(lineText).scope

      if (scope.isValid && lineCaretOffset == scope.range.last) {
        document.putUserData(MOVE_CARET, Unit)
        return true
      }
    }

    @Suppress("DEPRECATION")
    return super.isEnabled(editor, dataContext)
  }
}
