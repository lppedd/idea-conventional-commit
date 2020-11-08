package com.github.lppedd.cc

import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
private class CCApplicationLoadListener : ApplicationLoadListener {
  override fun beforeApplicationLoaded(application: Application, configPath: String) {
    PluginInstaller.addStateListener(CCPluginUninstallListener())
  }
}
