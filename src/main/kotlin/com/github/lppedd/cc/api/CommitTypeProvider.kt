package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitTypeProvider : CommitTokenProvider {
  /**
   * Returns commit types, optionally based on a user-inputted prefix.
   *
   * @param prefix a prefix to optionally filter down types, or an empty string
   */
  fun getCommitTypes(prefix: String): Collection<CommitType>
}
