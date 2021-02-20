package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.isCommitMessage
import com.intellij.codeInsight.actions.ReformatCodeAction
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.vcs.commit.message.ReformatCommitMessageAction

/**
 * Prioritizes [ReformatCommitMessageAction] over [ReformatCodeAction].
 * This is required to allow reformatting using the keys shortcut in every IDEA release.
 *
 * @author Edoardo Luppi
 */
@Compatibility(description = "See IDEA-254830")
private class ReformatActionPromoter : ActionPromoter {
  override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
    if (isApplicable(context)) {
      actions.sortedWith(AnActionComparator)
    } else {
      actions.toMutableList()
    }

  private fun isApplicable(context: DataContext): Boolean {
    val editor = context.getData(CommonDataKeys.EDITOR) ?: return false
    return editor.document.isCommitMessage()
  }

  private object AnActionComparator : Comparator<AnAction> {
    override fun compare(a1: AnAction, a2: AnAction): Int =
      when {
        a1 is ReformatCommitMessageAction -> -1
        a2 is ReformatCommitMessageAction -> 1
        else -> 0
      }
  }
}
