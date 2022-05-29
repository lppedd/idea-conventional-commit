package com.github.lppedd.cc.api

/**
 * Represents a page inside the "What's new" dialog.
 *
 * @author Edoardo Luppi
 * @see WhatsNewProvider
 * @see com.github.lppedd.cc.whatsnew.WhatsNewDialog
 */
interface WhatsNewPage {
  /** E.g. `0.19.0` */
  fun getVersion(): String?

  /** E.g. `0_19_0.html` */
  fun getFileName(): String
}
