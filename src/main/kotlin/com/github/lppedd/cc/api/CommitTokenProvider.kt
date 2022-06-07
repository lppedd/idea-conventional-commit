package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * Provides commit tokens to be proposed to the user via UI.
 *
 * @author Edoardo Luppi
 */
@Experimental
interface CommitTokenProvider {
  /** Returns the provider's global unique identifier. */
  fun getId(): String

  /** Returns the provider's UI presentation options. */
  fun getPresentation(): ProviderPresentation
}
