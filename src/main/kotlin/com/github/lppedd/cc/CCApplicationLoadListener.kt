package com.github.lppedd.cc

import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application
import com.intellij.openapi.project.ProjectManager
import java.nio.file.Path

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
private class CCApplicationLoadListener : ApplicationLoadListener {
  override fun beforeApplicationLoaded(application: Application, configPath: Path) {
    subscribeToProjectOpened(application)
    PluginInstaller.addStateListener(CCPluginUninstallListener())
  }

  private fun subscribeToProjectOpened(application: Application) {
    application.messageBus.connect().subscribe(
        ProjectManager.TOPIC,
        CCProjectManagerListener(),
    )
  }
}
