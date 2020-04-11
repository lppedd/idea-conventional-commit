package com.github.lppedd.cc.collection

/**
 * @author Edoardo Luppi
 */
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
