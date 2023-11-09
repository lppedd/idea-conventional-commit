package com.github.lppedd.cc.api

/**
 * @author Edoardo Luppi
 */
public interface WhatsNewProviderService {
  /**
   * Returns all the registered "What's new" providers.
   */
  public fun getWhatsNewProviders(): Collection<WhatsNewProvider>
}
