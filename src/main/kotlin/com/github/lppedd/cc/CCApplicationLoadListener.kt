package com.github.lppedd.cc

import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application
import com.intellij.openapi.project.ProjectManager

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
private class CCApplicationLoadListener : ApplicationLoadListener {
  override fun beforeApplicationLoaded(application: Application, configPath: String) {
    subscribeToProjectOpened(application)
    PluginInstaller.addStateListener(CCPluginUninstallListener())
  }

  private fun subscribeToProjectOpened(application: Application) {
    @Suppress("IncorrectParentDisposable")
    application.messageBus.connect(application).subscribe(
      ProjectManager.TOPIC,
      CCProjectManagerListener(),
    )
  }
}
