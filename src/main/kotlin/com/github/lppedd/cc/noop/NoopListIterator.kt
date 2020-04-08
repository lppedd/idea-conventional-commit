package com.github.lppedd.cc.noop

import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal object NoopListIterator : MutableListIterator<Any?> {
  override fun hasNext(): Boolean = false
  override fun hasPrevious(): Boolean = false
  override fun next(): Any? = null
  override fun nextIndex(): Int = -1
  override fun previous(): Any? = null
  override fun previousIndex(): Int = -1
  override fun add(element: Any?) {}
  override fun remove() {}
  override fun set(element: Any?) {}
}
