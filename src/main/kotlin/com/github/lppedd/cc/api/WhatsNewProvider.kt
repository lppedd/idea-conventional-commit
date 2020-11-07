package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.AbstractExtensionPointBean
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.util.xmlb.annotations.*
import org.jetbrains.annotations.ApiStatus.*

@JvmSynthetic
internal val WHATS_NEW_EP = ExtensionPointName<WhatsNewProvider>(
  "com.github.lppedd.idea-conventional-commit.whatsNewProvider"
)

/**
 * An entry point to provide your own "what's new" pages for the What's New dialog.
 * The provided pages will be displayed in a separate tab named after [displayName].
 *
 * Example usage:
 * ```
 * <whatsNewProvider implementation="my.package.MyWhatsNewProvider">
 *   <files>
 *     <name version="0.16.0">0_16_0.html</name>
 *     <name version="0.15.3">0_15_3.html</name>
 *   </files>
 * </whatsNewProvider>
 * ```
 *
 * @author Edoardo Luppi
 * @see com.github.lppedd.cc.whatsnew.WhatsNewDialog
 */
@Experimental
@AvailableSince("0.16.0")
abstract class WhatsNewProvider : AbstractExtensionPointBean() {
  @Property(surroundWithTag = false)
  var files: WhatsNewFiles = WhatsNewFiles()

  /**
   * The name for the dialog's tab.
   */
  abstract fun displayName(): String

  /**
   * States if the "what's new" pages should be displayed at IDE startup,
   * and thus states if the What's New dialog should be shown.
   * Typically the dialog should be shown every plugin update.
   */
  abstract fun shouldDisplay(): Boolean

  @Tag("files")
  data class WhatsNewFiles(
      @Attribute("basePath")
      var basePath: String = "/whatsnew/",

      @Property(surroundWithTag = false)
      @XCollection(elementName = "name", valueAttributeName = "")
      var fileDescriptions: Set<FileDescription> = LinkedHashSet(),
  )

  @Tag("name")
  data class FileDescription(
      @Attribute("version")
      var version: String? = null,

      @Text
      var name: String = "",
  )
}
