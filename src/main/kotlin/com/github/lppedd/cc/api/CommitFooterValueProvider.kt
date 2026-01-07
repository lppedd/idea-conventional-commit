package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
public interface CommitFooterValueProvider : CommitTokenProvider {
  /**
   * Returns commit footer values, optionally considering other user-inputted tokens.
   *
   * @param footerType A footer type to optionally filter down footer values, or an empty string
   * @param type A type to optionally filter down footer values, or an empty string
   * @param scope A subject to optionally filter down footer values, or an empty string
   * @param subject A subject to optionally filter down footer values, or an empty string
   */
  public fun getCommitFooterValues(
    footerType: String,
    type: String?,
    scope: String?,
    subject: String?,
  ): Collection<CommitFooterValue>
}
