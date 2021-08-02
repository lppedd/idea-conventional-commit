package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginAware
import com.intellij.openapi.extensions.PluginDescriptor
import org.jetbrains.annotations.ApiStatus.*

@JvmSynthetic
internal val WHATS_NEW_EP = ExtensionPointName<WhatsNewProvider>(
    "com.github.lppedd.idea-conventional-commit.whatsNewProvider"
)

/**
 * An entry point to provide your own "what's new" pages for the What's New dialog.
 * The provided pages will be displayed in a separate tab named after [getDisplayName].
 *
 * @author Edoardo Luppi
 * @see com.github.lppedd.cc.whatsnew.WhatsNewDialog
 */
@Experimental
@AvailableSince("0.16.0")
abstract class WhatsNewProvider : PluginAware {
  lateinit var pluginDescriptor: PluginDescriptor
    private set

  /**
   * The name for the dialog's tab.
   */
  abstract fun getDisplayName(): String

  /**
   * States if the "what's new" pages should be displayed at IDE startup,
   * and thus states if the What's New dialog should be shown.
   * Typically, the dialog should be shown on every plugin update.
   */
  abstract fun shouldDisplay(): Boolean

  /**
   * The base path where [WhatsNewPage.fileName]s are located.
   */
  abstract fun basePath(): String

  /**
   * The what's new pages to be displayed.
   *
   * The lowest index represents the newest page and the highest index
   * represents the oldest one.
   */
  abstract fun getWhatsNewPages(): List<WhatsNewPage>

  override fun setPluginDescriptor(pluginDescriptor: PluginDescriptor) {
    this.pluginDescriptor = pluginDescriptor
  }

  data class WhatsNewPage(
      /** E.g. `0.19.0` */
      var version: String? = null,

      /** E.g. `0_19_0.html` */
      var fileName: String,
  )
}
