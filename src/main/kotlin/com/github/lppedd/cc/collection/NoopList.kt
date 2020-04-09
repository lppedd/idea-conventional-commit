package com.github.lppedd.cc.collection

import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal object NoopList : MutableList<Any?> {
  override val size: Int = 0
  override fun contains(element: Any?): Boolean = false
  override fun containsAll(elements: Collection<Any?>): Boolean = false
  override fun get(index: Int): Any? = null
  override fun isEmpty(): Boolean = true
  override fun lastIndexOf(element: Any?): Int = -1
  override fun addAll(elements: Collection<Any?>): Boolean = false
  override fun clear() {}
  override fun removeAll(elements: Collection<Any?>): Boolean = false
  override fun add(element: Any?): Boolean = false
  override fun add(index: Int, element: Any?) {}
  override fun remove(element: Any?): Boolean = false
  override fun set(index: Int, element: Any?): Any? = null
  override fun retainAll(elements: Collection<Any?>): Boolean = false
  override fun subList(fromIndex: Int, toIndex: Int): MutableList<Any?> = this
  override fun indexOf(element: Any?): Int = -1
  override fun addAll(index: Int, elements: Collection<Any?>): Boolean = false
  override fun iterator(): MutableIterator<Any?> = NoopIterator
  override fun listIterator(): MutableListIterator<Any?> = NoopListIterator
  override fun listIterator(index: Int): MutableListIterator<Any?> = NoopListIterator
  override fun removeAt(index: Int): Any? = null
}
