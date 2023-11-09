package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
public interface CommitTypeProvider : CommitTokenProvider {
  /**
   * Returns commit types, optionally based on a user-inputted prefix.
   *
   * @param prefix A prefix to optionally filter down types, or an empty string
   */
  public fun getCommitTypes(prefix: String): Collection<CommitType>
}
