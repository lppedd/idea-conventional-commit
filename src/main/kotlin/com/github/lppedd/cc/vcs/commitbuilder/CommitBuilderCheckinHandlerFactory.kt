package com.github.lppedd.cc.vcs.commitbuilder

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory

/**
 * Handles successful commits to clean up saved values of [CommitBuilderDialog].
 *
 * @author Edoardo Luppi
 */
internal class CommitBuilderCheckinHandlerFactory : CheckinHandlerFactory() {
  override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler =
    MyCheckinHandler(panel.project)

  private class MyCheckinHandler(private val project: Project) : CheckinHandler() {
    override fun checkinSuccessful() {
      CommitBuilderService.getInstance(project).clear()
      super.checkinSuccessful()
    }
  }
}
