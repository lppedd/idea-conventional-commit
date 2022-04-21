package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.ide.ApplicationLoadListener
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.openapi.application.Application
import com.intellij.openapi.project.ProjectManager
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
private class CCApplicationLoadListener : ApplicationLoadListener {
  @Compatibility(description = """
    This method's signature is used by newer version of the Platform.
    beforeApplicationLoaded(Application, String) has been removed starting from 221.3427.89
  """)
  fun beforeApplicationLoaded(application: Application, configPath: Path) {
    beforeApplicationLoaded(application, configPath.absolutePathString())
  }

  override fun beforeApplicationLoaded(application: Application, configPath: String) {
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
