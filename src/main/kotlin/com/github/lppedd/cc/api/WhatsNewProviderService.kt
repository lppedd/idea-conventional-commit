package com.github.lppedd.cc.api

import com.intellij.openapi.components.service

/**
 * @author Edoardo Luppi
 */
public interface WhatsNewProviderService {
  public companion object {
    @JvmStatic
    public fun getInstance(): WhatsNewProviderService = service()
  }

  /**
   * Returns all registered "What's new" providers.
   */
  public fun getWhatsNewProviders(): Collection<WhatsNewProvider>
}
