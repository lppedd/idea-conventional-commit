package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.isCommitMessage
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext

/**
 * Prioritizes [CommitBuilderAction] in case of collision with other actions' shortcuts.
 *
 * @author Edoardo Luppi
 */
internal class CommitBuilderActionPromoter : ActionPromoter {
  override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
    if (isApplicable(context)) {
      actions.sortedWith(CommitBuilderActionComparator)
    } else {
      actions.toMutableList()
    }

  private fun isApplicable(context: DataContext): Boolean {
    val editor = context.getData(CommonDataKeys.EDITOR) ?: return false
    return editor.document.isCommitMessage()
  }

  private object CommitBuilderActionComparator : Comparator<AnAction> {
    override fun compare(a1: AnAction, a2: AnAction): Int =
      when {
        a1 is CommitBuilderAction -> -1
        a2 is CommitBuilderAction -> 1
        else -> 0
      }
  }
}
