package com.github.lppedd.cc

import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application
import java.nio.file.Path

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CCApplicationLoadListener : ApplicationLoadListener {
  override suspend fun beforeApplicationLoaded(application: Application, configPath: Path) {
    PluginInstaller.addStateListener(CCPluginStateListener())
  }
}
