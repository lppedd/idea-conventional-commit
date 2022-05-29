package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitTypeProvider : CommitTokenProvider {
  /**
   * Returns commit types, optionally based on a prefix.
   *
   * @param prefix a prefix to optionally filter down commit types.
   *    The prefix depends on what the user inputted before invoking completion
   */
  fun getCommitTypes(prefix: String): Collection<CommitType>
}
