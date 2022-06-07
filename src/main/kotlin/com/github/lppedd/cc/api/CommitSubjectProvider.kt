package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitSubjectProvider : CommitTokenProvider {
  /**
   * Returns commit subjects, optionally considering the user-inputted type and scope.
   *
   * @param commitType a type to optionally filter down subjects, or an empty string
   * @param commitScope a scope to optionally filter down subjects, or an empty string
   */
  fun getCommitSubjects(commitType: String, commitScope: String): Collection<CommitSubject>
}
