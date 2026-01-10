package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CC
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys

/**
 * @author Edoardo Luppi
 */
internal class ClearLocalMessageHistoryAction : DumbAwareAction() {
  init {
    isEnabledInModalContext = true
    templatePresentation.icon = CC.Icon.General.ClearMessageHistory
  }

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.EDT

  override fun update(event: AnActionEvent) {
    event.presentation.isEnabledAndVisible = false
    val project = event.project

    if (project != null && getCommitMessagePanel(event) != null) {
      val recentCommitsService = RecentCommitsService.getInstance(project)
      val isVisible = !recentCommitsService.getLocalMessageHistory().isEmpty()
      event.presentation.isEnabledAndVisible = isVisible
    }
  }

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val recentCommitsService = RecentCommitsService.getInstance(project)
    recentCommitsService.clearLocalMessageHistory()
  }

  private fun getCommitMessagePanel(event: AnActionEvent): CommitMessageI? =
    event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
}
