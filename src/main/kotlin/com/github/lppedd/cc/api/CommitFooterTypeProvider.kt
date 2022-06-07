package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitFooterTypeProvider : CommitTokenProvider {
  /** Returns commit footer types. */
  fun getCommitFooterTypes(): Collection<CommitFooterType>
}
