package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.PluginAware
import com.intellij.openapi.extensions.PluginDescriptor
import org.jetbrains.annotations.ApiStatus.*

/**
 * An entry point to provide your own "What's new" pages for the What's New dialog.
 * The provided pages will be displayed in a separate tab named after [getDisplayName].
 *
 * @author Edoardo Luppi
 * @see com.github.lppedd.cc.whatsnew.WhatsNewDialog
 */
@Experimental
interface WhatsNewProvider : PluginAware {
  /**
   * Returns the name to be displayed in the "What's new" dialog's tab
   * representing this provider.
   */
  fun getDisplayName(): String

  /**
   * Returns if the "What's new" pages should be displayed at IDE startup,
   * and thus returns if the "What's new" dialog should be shown.
   *
   * Typically, the dialog should be shown on every plugin update.
   */
  fun shouldDisplayAtStartup(): Boolean

  /** Returns the base path where [WhatsNewPage.getFileName] (s) are located. */
  fun getBasePath(): String

  /**
   * Returns the "What's new" pages to be displayed.
   *
   * The lowest index (`0`) represents the newest page and the highest index
   * represents the oldest one.
   */
  fun getPages(): Collection<WhatsNewPage>

  /** @see PluginAware.setPluginDescriptor */
  fun getPluginDescriptor(): PluginDescriptor
}
