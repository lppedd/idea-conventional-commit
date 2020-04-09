package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal interface ProviderWrapper : CommitTokenProvider {
  fun getPriority(): Priority
}

internal inline class Priority(val value: Int) : Comparable<Priority> {
  override fun compareTo(other: Priority): Int =
    value.compareTo(other.value)

  operator fun times(times: Int): Int =
    value * times
}
