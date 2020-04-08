package com.github.lppedd.cc.noop

import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal object NoopIterator : MutableIterator<Any?> {
  override fun hasNext(): Boolean = false
  override fun next(): Any? = null
  override fun remove() {}
}
