package com.github.lppedd.cc.api

import com.github.lppedd.cc.whatsnew.WhatsNewDialog

/**
 * Represents a page inside the "What's new" dialog.
 *
 * @author Edoardo Luppi
 * @see WhatsNewProvider
 * @see WhatsNewDialog
 */
public interface WhatsNewPage {
  /**
   * Returns the product version associated with the What's New page.
   *
   * Example: `0.19.0`
   */
  public fun getVersion(): String?

  /**
   * Returns the name of the file which contains the What's new page data.
   *
   * The name is resolved relative to [WhatsNewProvider.getBasePath].
   *
   * Example: `0_19_0.html`
   */
  public fun getFileName(): String
}
