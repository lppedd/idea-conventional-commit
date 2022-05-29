package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.api.WhatsNewProvider
import com.github.lppedd.cc.api.WhatsNewProviderService
import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Internal
internal class InternalWhatsNewProviderService : WhatsNewProviderService {
  private companion object {
    val whatsNewEpName: ExtensionPointName<WhatsNewProvider> =
      ExtensionPointName("com.github.lppedd.idea-conventional-commit.whatsNewProvider")
  }

  override fun getWhatsNewProviders(): List<WhatsNewProvider> =
    whatsNewEpName.extensionList
}
