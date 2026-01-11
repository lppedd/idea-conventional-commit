package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus

/**
 * Provides commit tokens to be proposed to the user via UI.
 *
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
public interface CommitTokenProvider {
  /**
   * Returns the provider's global unique identifier.
   */
  public fun getId(): String

  /**
   * Returns the provider's UI presentation options.
   */
  public fun getPresentation(): ProviderPresentation
}
