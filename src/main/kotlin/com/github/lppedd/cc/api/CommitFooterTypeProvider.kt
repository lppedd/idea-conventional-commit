package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
public interface CommitFooterTypeProvider : CommitTokenProvider {
  /**
   * Returns commit footer types.
   */
  public fun getCommitFooterTypes(): Collection<CommitFooterType>
}
