package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitFooterValueProvider : CommitTokenProvider {
  /**
   * Returns commit footer values, optionally considering other user-inputted tokens.
   *
   * @param footerType a footer type to optionally filter down footer values, or an empty string
   * @param commitType a type to optionally filter down footer values, or an empty string
   * @param commitScope a subject to optionally filter down footer values, or an empty string
   * @param commitSubject a subject to optionally filter down footer values, or an empty string
   */
  fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue>
}
