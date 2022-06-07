package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitScopeProvider : CommitTokenProvider {
  /**
   * Returns commit scopes, optionally considering the user-inputted commit type.
   *
   * @param commitType a type to optionally filter down scopes, or an empty string
   */
  fun getCommitScopes(commitType: String): Collection<CommitScope>
}
