package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitBodyProvider : CommitTokenProvider {
  fun getCommitBodies(
      commitType: String,
      commitScope: String,
      commitSubject: String,
  ): Collection<CommitBody>
}
