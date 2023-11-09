package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.api.WhatsNewProvider
import com.github.lppedd.cc.api.WhatsNewProviderService
import com.intellij.openapi.extensions.ExtensionPointName

/**
 * @author Edoardo Luppi
 */
internal class InternalWhatsNewProviderService : WhatsNewProviderService {
  private companion object {
    private val whatsNewEpName: ExtensionPointName<WhatsNewProvider> =
      ExtensionPointName("com.github.lppedd.idea-conventional-commit.whatsNewProvider")
  }

  override fun getWhatsNewProviders(): Collection<WhatsNewProvider> =
    whatsNewEpName.extensionList
}
