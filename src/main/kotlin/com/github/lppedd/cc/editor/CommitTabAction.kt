package com.github.lppedd.cc.editor

import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.getCaretOffset
import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.github.lppedd.cc.language.psi.*
import com.github.lppedd.cc.moveCaretRelatively
import com.github.lppedd.cc.scheduleAutoPopup
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actions.TabAction
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager

/**
 * Allows jumping over certain message elements with the Tab key.
 * Currently, the following elements are supported:
 *   - opening scope parenthesis
 *   - closing scope parenthesis
 *   - subject and footer separator
 *
 * @author Edoardo Luppi
 */
internal class CommitTabAction : TabAction() {
  init {
    setupHandler(CommitTabHandler)
  }

  private object CommitTabHandler : Handler() {
    private val moveCaretKey = Key.create<Int>("Vcs.CommitMessage.moveCaret")

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
      val steps = editor.getUserData(moveCaretKey)

      if (steps != null) {
        editor.putUserData(moveCaretKey, null)
        editor.moveCaretRelatively(steps)
        editor.scheduleAutoPopup()
      } else {
        super.executeWriteAction(editor, caret, dataContext)
      }
    }

    @Suppress("override_deprecation")
    override fun isEnabled(editor: Editor, dataContext: DataContext): Boolean {
      val project = editor.project

      if (project != null) {
        val psiFile = project.service<PsiDocumentManager>().getPsiFile(editor.document)
        val elementAtCaret = psiFile?.findElementAt(editor.getCaretOffset())

        if (elementAtCaret is ConventionalCommitScopeOpenParenPsiElement ||
            elementAtCaret is ConventionalCommitScopeCloseParenPsiElement ||
            elementAtCaret is ConventionalCommitBreakingChangePsiElement) {
          editor.putUserData(moveCaretKey, 1)
          return true
        } else if (elementAtCaret is ConventionalCommitSeparatorPsiElement) {
          // Let's find where the subject/footer value text begins,
          // or just place the cursor at the right place if it doesn't exist yet
          val nextSibling = elementAtCaret.nextSibling

          if (nextSibling is ConventionalCommitSubjectPsiElement ||
              nextSibling is ConventionalCommitFooterValuePsiElement) {
            val text = nextSibling.text
            editor.putUserData(moveCaretKey, if (text[0].isWhitespace()) 2 else 1)
          } else {
            editor.putUserData(moveCaretKey, 1)
          }

          return true
        }
      }

      @Suppress("deprecation")
      return super.isEnabled(editor, dataContext)
    }

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
      val project = editor.project

      if (project != null) {
        val psiFile = project.service<PsiDocumentManager>().getPsiFile(editor.document)

        if (psiFile != null) {
          val elementAtCaret = psiFile.findElementAt(caret.offset)

          if (elementAtCaret != null && elementAtCaret.language.isKindOf(ConventionalCommitLanguage)) {
            return true
          }
        }
      }

      return super.isEnabledForCaret(editor, caret, dataContext)
    }
  }
}
