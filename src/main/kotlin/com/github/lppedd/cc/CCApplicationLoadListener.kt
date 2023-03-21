package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * @author Edoardo Luppi
 */
internal class CCApplicationLoadListener : ApplicationLoadListener {
  override fun beforeApplicationLoaded(application: Application, configPath: String) {
    PluginInstaller.addStateListener(CCPluginStateListener())
  }
}
