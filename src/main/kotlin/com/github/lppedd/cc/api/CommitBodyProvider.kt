package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitBodyProvider : CommitTokenProvider {
  /**
   * Returns commit bodies, optionally considering other user-inputted tokens.
   *
   * @param commitType a type to optionally filter down bodies, or an empty string
   * @param commitScope a scope to optionally filter down bodies, or an empty string
   * @param commitSubject a subject to optionally filter down bodies, or an empty string
   */
  fun getCommitBodies(
      commitType: String,
      commitScope: String,
      commitSubject: String,
  ): Collection<CommitBody>
}
