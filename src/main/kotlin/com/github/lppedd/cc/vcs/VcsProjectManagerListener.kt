package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CC
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsListener

/**
 * @author Edoardo Luppi
 */
internal class VcsProjectManagerListener : ProjectManagerListener {
  override fun projectOpened(project: Project) {
    val vcsConfigListener = VcsListener {
      if (Registry.`is`(CC.Registry.VcsEnabled, false)) {
        ApplicationManager.getApplication().executeOnPooledThread {
          val vcsHandler = project.service<VcsService>()
          vcsHandler.refresh()
        }
      }
    }

    val busConnection = project.messageBus.connect()
    busConnection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, vcsConfigListener)
    busConnection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED_IN_PLUGIN, vcsConfigListener)
  }
}
