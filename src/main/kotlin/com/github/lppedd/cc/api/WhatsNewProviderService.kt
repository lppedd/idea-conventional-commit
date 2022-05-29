package com.github.lppedd.cc.api

/**
 * @author Edoardo Luppi
 */
interface WhatsNewProviderService {
  /** Returns all the registered "What's new" providers. */
  fun getWhatsNewProviders(): List<WhatsNewProvider>
}
