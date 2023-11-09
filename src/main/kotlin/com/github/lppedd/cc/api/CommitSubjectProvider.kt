package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
public interface CommitSubjectProvider : CommitTokenProvider {
  /**
   * Returns commit subjects, optionally considering the user-inputted type and scope.
   *
   * @param type A type to optionally filter down subjects, or an empty string
   * @param scope A scope to optionally filter down subjects, or an empty string
   */
  public fun getCommitSubjects(type: String, scope: String): Collection<CommitSubject>
}
