package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitSubjectProvider : CommitTokenProvider {
  fun getCommitSubjects(commitType: String, commitScope: String): Collection<CommitSubject>
}
