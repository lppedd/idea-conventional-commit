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
public interface WhatsNewProvider : PluginAware {
  /**
   * Returns the name - representing this provider - to be displayed in a "What's new" dialog's tab.
   */
  public fun getDisplayName(): String

  /**
   * Returns if the "What's new" pages should be displayed at IDE startup,
   * and thus returns if the "What's new" dialog should be shown.
   *
   * Typically, the dialog should be shown on every plugin update.
   */
  public fun shouldDisplayAtStartup(): Boolean

  /**
   * Returns the path where [WhatsNewPage.getFileName]s are located.
   */
  public fun getBasePath(): String

  /**
   * Returns the "What's new" pages to be displayed.
   *
   * The lowest collection index `0` represents the newest page and the highest index represents the oldest one.
   */
  public fun getPages(): Collection<WhatsNewPage>

  /**
   * @see PluginAware.setPluginDescriptor
   */
  public fun getPluginDescriptor(): PluginDescriptor
}
