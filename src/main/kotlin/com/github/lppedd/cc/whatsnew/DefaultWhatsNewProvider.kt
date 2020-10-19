package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.api.WhatsNewProvider
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.extensions.PluginId

/**
 * Provides the changelogs for the Conventional Commit core plugin.
 *
 * @author Edoardo Luppi
 */
private class DefaultWhatsNewProvider : WhatsNewProvider() {
  private val versionPropertyName = "com.github.lppedd.cc.version"

  override fun displayName(): String =
    "Core"

  override fun shouldDisplay(): Boolean {
    val properties = PropertiesComponent.getInstance()
    val pluginInstalledVersion = getPlugin()?.version ?: return false
    val pluginRegisteredVersion = properties.getValue(versionPropertyName)

    if (pluginInstalledVersion == pluginRegisteredVersion) {
      return false
    }

    properties.setValue(versionPropertyName, pluginInstalledVersion)
    return true
  }

  private fun getPlugin(): IdeaPluginDescriptor? =
    PluginManager.getPlugin(PluginId.findId("com.github.lppedd.idea-conventional-commit"))
}
