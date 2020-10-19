package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.AbstractExtensionPointBean
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.util.xmlb.annotations.*
import org.jetbrains.annotations.ApiStatus

internal val WHATS_NEW_EP = ExtensionPointName<WhatsNewProvider>(
  "com.github.lppedd.idea-conventional-commit.whatsNewProvider"
)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
abstract class WhatsNewProvider : AbstractExtensionPointBean() {
  @Property(surroundWithTag = false)
  var files: WhatsNewFiles = WhatsNewFiles()

  abstract fun displayName(): String
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
