package com.github.lppedd.cc.collection

/**
 * @author Edoardo Luppi
 */
internal object NoopIterator : MutableIterator<Any?> {
  override fun hasNext(): Boolean = false
  override fun next(): Any? = null
  override fun remove() {}
}
