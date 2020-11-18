package com.github.lppedd.cc

import com.github.lppedd.cc.vcs.CCVcsHandler
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
internal class CCProjectManagerListener : ProjectManagerListener {
  override fun projectOpened(project: Project) {
    val vcsConfigListener = VcsListener {
      @Suppress("UnresolvedPluginConfigReference")
      if (Registry.`is`("com.github.lppedd.cc.providers.vcs", false).not()) {
        return@VcsListener
      }

      ApplicationManager.getApplication().executeOnPooledThread {
        val vcsHandler = project.service<CCVcsHandler>()
        vcsHandler.reset()
      }
    }

    @Suppress("IncorrectParentDisposable")
    project.messageBus.connect(project).also {
      it.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, vcsConfigListener)
      it.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED_IN_PLUGIN, vcsConfigListener)
    }
  }
}
