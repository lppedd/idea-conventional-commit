package com.github.lppedd.cc.api

import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitFooterTypeProvider : CommitTokenProvider {
  fun getCommitFooterTypes(): Collection<CommitFooterType>
}
