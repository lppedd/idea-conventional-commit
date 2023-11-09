package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
public interface CommitFooterTypeProvider : CommitTokenProvider {
  /**
   * Returns commit footer types.
   */
  public fun getCommitFooterTypes(): Collection<CommitFooterType>
}
