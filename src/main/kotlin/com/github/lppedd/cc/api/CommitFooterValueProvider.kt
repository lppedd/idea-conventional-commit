package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitFooterValueProvider : CommitTokenProvider {
  fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue>
}
