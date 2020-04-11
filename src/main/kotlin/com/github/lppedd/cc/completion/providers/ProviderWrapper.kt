package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.Priority

/**
 * @author Edoardo Luppi
 */
internal interface ProviderWrapper : CommitTokenProvider {
  fun getPriority(): Priority
}
