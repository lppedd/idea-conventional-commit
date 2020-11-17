package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.codeInsight.actions.ReformatCodeAction
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.vcs.commit.message.ReformatCommitMessageAction

/**
 * Prioritizes [ReformatCommitMessageAction] over [ReformatCodeAction].
 * This is required to allow reformatting using the keys shortcut in every IDEA release.
 *
 * @author Edoardo Luppi
 */
@Compatibility(minVersion = "See IDEA-254830")
private class ReformatActionPromoter : ActionPromoter {
  override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
    if (isApplicable(context)) {
      actions.sortedWith(AnActionComparator)
    } else {
      actions.toMutableList()
    }

  private fun isApplicable(context: DataContext): Boolean =
    (context.getData("editor") as? Editor)
      ?.document
      ?.getUserData(CommitMessage.DATA_KEY) != null

  private object AnActionComparator : Comparator<AnAction> {
    override fun compare(a1: AnAction, a2: AnAction): Int =
      when {
        a1 is ReformatCommitMessageAction -> -1
        a2 is ReformatCommitMessageAction -> 1
        else -> 0
      }
  }
}
