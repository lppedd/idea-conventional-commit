package com.github.lppedd.cc.completion

/**
 * @author Edoardo Luppi
 */
internal inline class Priority(val value: Int) : Comparable<Priority> {
  override fun compareTo(other: Priority): Int =
    value.compareTo(other.value)

  operator fun plus(other: Int): Int =
    value + other

  operator fun times(times: Int): Int =
    value * times
}
