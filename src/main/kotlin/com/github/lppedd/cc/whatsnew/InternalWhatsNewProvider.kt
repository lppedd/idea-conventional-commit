package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.WhatsNewPage
import com.github.lppedd.cc.api.WhatsNewProvider
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import java.util.*
import kotlin.math.max

/**
 * Provides the changelogs for the Conventional Commit core plugin.
 *
 * @author Edoardo Luppi
 */
internal class InternalWhatsNewProvider : WhatsNewProvider {
  companion object {
    const val PROPERTY_VERSION = "com.github.lppedd.cc.version"
  }

  private lateinit var pluginDescriptor: PluginDescriptor

  private val whatsNewPages = listOf(
      DefaultWhatsNewPage("0.21.0", "0_21_0.html"),
      DefaultWhatsNewPage("0.20.1", "0_20_1.html"),
      DefaultWhatsNewPage("0.20.0", "0_20_0.html"),
      DefaultWhatsNewPage("0.19.0", "0_19_0.html"),
      DefaultWhatsNewPage("0.18.0", "0_18_0.html"),
      DefaultWhatsNewPage("0.17.0", "0_17_0.html"),
  )

  override fun getPluginDescriptor(): PluginDescriptor =
    pluginDescriptor

  override fun setPluginDescriptor(pluginDescriptor: PluginDescriptor) {
    this.pluginDescriptor = pluginDescriptor
  }

  override fun getDisplayName(): String =
    "Core"

  override fun shouldDisplayAtStartup(): Boolean {
    val properties = PropertiesComponent.getInstance()
    val installedVersion = getPlugin()?.version ?: return false
    val registeredVersion = properties.getValue(PROPERTY_VERSION, "0.0.0")

    if (PluginVersion(installedVersion) > PluginVersion(registeredVersion)) {
      properties.setValue(PROPERTY_VERSION, installedVersion)
      val showOnEveryUpdate = properties.getValue(WhatsNewDialog.PROPERTY_SHOW, "true").toBoolean()
      return showOnEveryUpdate && getPages().any { it.getVersion() == installedVersion }
    }

    return false
  }

  override fun getBasePath(): String =
    "/whatsnew/"

  override fun getPages(): List<WhatsNewPage> =
    whatsNewPages

  private fun getPlugin(): IdeaPluginDescriptor? =
    PluginManagerCore.getPlugin(PluginId.findId(CC.PluginId))

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

  private class DefaultWhatsNewPage(
      private val version: String,
      private val fileName: String,
  ) : WhatsNewPage {
    override fun getVersion(): String =
      version

    override fun getFileName(): String =
      fileName
  }
}
