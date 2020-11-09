package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.WhatsNewProvider
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.extensions.PluginId
import java.util.*
import kotlin.math.max

/**
 * Provides the changelogs for the Conventional Commit core plugin.
 *
 * @author Edoardo Luppi
 */
internal class DefaultWhatsNewProvider : WhatsNewProvider() {
  companion object {
    const val PROPERTY_VERSION = "com.github.lppedd.cc.version"
  }

  override fun displayName(): String =
    "Core"

  override fun shouldDisplay(): Boolean {
    val properties = PropertiesComponent.getInstance()
    val installedVersion = getPlugin()?.version ?: return false
    val registeredVersion = properties.getValue(PROPERTY_VERSION, "0.0.0")

    if (PluginVersion(installedVersion) > PluginVersion(registeredVersion)) {
      properties.setValue(PROPERTY_VERSION, installedVersion)
      val showOnEveryUpdate = properties.getValue(WhatsNewPanel.PROPERTY_SHOW, "true").toBoolean()
      val hasAttachedFile = files.fileDescriptions.any { it.version == installedVersion }
      return showOnEveryUpdate && hasAttachedFile
    }

    return false
  }

  private fun getPlugin(): IdeaPluginDescriptor? =
    PluginManager.getPlugin(PluginId.findId(CC.PluginId))

  private class PluginVersion(version: String) : Comparable<PluginVersion> {
    private val parts = version.split(".").map(String::toInt)

    init {
      require(parts.isNotEmpty()) { "The plugin version is invalid" }
    }

    override fun equals(other: Any?): Boolean {
      return compareTo(other as? PluginVersion ?: return false) == 0
    }

    override fun hashCode(): Int =
      Objects.hash(parts)

    override fun compareTo(other: PluginVersion): Int {
      val maxParts = max(parts.size, other.parts.size)

      for (i in 0 until maxParts) {
        val thisPart = if (i < parts.size) parts[i] else 0
        val otherPart = if (i < other.parts.size) other.parts[i] else 0
        if (thisPart < otherPart) return -1
        if (thisPart > otherPart) return 1
      }

      return 0
    }
  }
}
