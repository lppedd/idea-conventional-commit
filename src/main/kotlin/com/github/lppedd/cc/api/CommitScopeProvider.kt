package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
public interface CommitScopeProvider : CommitTokenProvider {
  /**
   * Returns commit scopes, optionally considering the user-inputted commit type.
   *
   * @param type A type to optionally filter down scopes, or an empty string
   */
  public fun getCommitScopes(type: String): Collection<CommitScope>
}
