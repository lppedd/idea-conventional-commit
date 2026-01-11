package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
public interface CommitBodyProvider : CommitTokenProvider {
  /**
   * Returns commit bodies, optionally considering other user-inputted tokens.
   *
   * @param type A type to optionally filter down bodies, or an empty string
   * @param scope A scope to optionally filter down bodies, or an empty string
   * @param subject A subject to optionally filter down bodies, or an empty string
   */
  public fun getCommitBodies(type: String, scope: String, subject: String): Collection<CommitBody>
}
