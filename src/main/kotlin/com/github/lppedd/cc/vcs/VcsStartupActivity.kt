package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CCRegistry
import com.github.lppedd.cc.runInBackgroundThread
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsListener

/**
 * @author Edoardo Luppi
 */
internal class VcsStartupActivity : ProjectActivity, DumbAware {
  override suspend fun execute(project: Project) {
    val vcsConfigListener = VcsListener {
      if (CCRegistry.isVcsSupportEnabled()) {
        runInBackgroundThread {
          val vcsHandler = VcsService.getInstance(project)
          vcsHandler.refresh()
        }
      }
    }

    val busConnection = project.messageBus.connect()
    busConnection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, vcsConfigListener)
    busConnection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED_IN_PLUGIN, vcsConfigListener)
  }
}
