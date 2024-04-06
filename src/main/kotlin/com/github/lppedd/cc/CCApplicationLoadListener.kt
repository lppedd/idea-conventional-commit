package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application
import java.nio.file.Path

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CCApplicationLoadListener : ApplicationLoadListener {
  // This method is called from 223.* to < 241.8102.
  // 241.8102.112 introduced a suspending alternative, which we have defined below
  @Compatibility(minVersion = "241.8102.112")
  override fun beforeApplicationLoaded(application: Application, configPath: Path) {
    PluginInstaller.addStateListener(CCPluginStateListener())
  }

  @JvmName("beforeApplicationLoaded")
  @Suppress("unused", "unused_parameter", "RedundantSuspendModifier")
  suspend fun beforeApplicationLoadedCompat(application: Application, configPath: Path) {
    PluginInstaller.addStateListener(CCPluginStateListener())
  }
}
