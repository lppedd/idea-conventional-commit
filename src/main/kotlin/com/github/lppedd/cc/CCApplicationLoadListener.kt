package com.github.lppedd.cc

import com.github.lppedd.cc.CCRegistry.RegistryKeyDescriptor
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
    addRegistryKeys()
    PluginInstaller.addStateListener(CCPluginUninstallListener())
  }

  private fun subscribeToProjectOpened(application: Application) {
    @Suppress("IncorrectParentDisposable")
    application.messageBus.connect(application).subscribe(
      ProjectManager.TOPIC,
      CCProjectManagerListener(),
    )
  }

  private fun addRegistryKeys() {
    CCRegistry.addKeys(
      RegistryKeyDescriptor(
        name = CC.Registry.VcsEnabled,
        description = "Enable/disable the new VCS Provider",
        defaultValue = "true",
        restartRequired = true,
        pluginId = CC.PluginId,
      ),
    )
  }
}
