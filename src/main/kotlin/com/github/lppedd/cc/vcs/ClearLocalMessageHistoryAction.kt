package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CCIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys

/**
 * @author Edoardo Luppi
 */
internal class ClearLocalMessageHistoryAction : DumbAwareAction() {
  init {
    isEnabledInModalContext = true
    templatePresentation.icon = CCIcons.General.ClearMessageHistory
  }

  override fun update(event: AnActionEvent) {
    event.presentation.isEnabledAndVisible = false
    val project = event.project

    if (project != null && getCommitMessagePanel(event) != null) {
      val recentCommitsService = project.service<RecentCommitsService>()
      val isVisible = !recentCommitsService.getLocalMessageHistory().isEmpty()
      event.presentation.isEnabledAndVisible = isVisible
    }
  }

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val recentCommitsService = project.service<RecentCommitsService>()
    recentCommitsService.clearLocalMessageHistory()
  }

  private fun getCommitMessagePanel(event: AnActionEvent): CommitMessageI? =
    event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
}
