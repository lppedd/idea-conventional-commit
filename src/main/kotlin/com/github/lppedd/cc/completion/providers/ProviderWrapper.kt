package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.Priority
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal interface ProviderWrapper : CommitTokenProvider {
  fun getPriority(): Priority
}
