package com.github.lppedd.cc

import com.intellij.ide.plugins.PluginInstaller
import com.intellij.ide.util.RunOnceUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class CCRegisterPluginUninstallCleanup : StartupActivity.DumbAware {
  override fun runActivity(project: Project) {
    RunOnceUtil.runOnceForApp("cc.application.plugin.cleanup") {
      PluginInstaller.addStateListener(CCPluginStateListener())
    }
  }
}