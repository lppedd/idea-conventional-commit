package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitScopeProvider : CommitTokenProvider {
  /**
   * Returns commit scopes, optionally considering a commit type.
   *
   * @param commitType a commit type to optionally filter down commit scopes
   */
  fun getCommitScopes(commitType: String): Collection<CommitScope>
}
