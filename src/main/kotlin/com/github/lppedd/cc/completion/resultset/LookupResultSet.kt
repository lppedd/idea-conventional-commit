package com.github.lppedd.cc.completion.resultset

import com.github.lppedd.cc.completion.FlatPrefixMatcher
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.impl.LookupImpl
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class LookupResultSet(private val lookup: LookupImpl) : ResultSet {
  private var prefixMatcher: PrefixMatcher = PrefixMatcher.ALWAYS_TRUE

  override fun addElement(lookupElement: LookupElement) {
    lookup.addItem(lookupElement, prefixMatcher)
  }

  override fun withPrefixMatcher(prefix: String): ResultSet {
    prefixMatcher = FlatPrefixMatcher(prefix)
    return this
  }

  override fun withPrefixMatcher(prefixMatcher: PrefixMatcher): ResultSet {
    this.prefixMatcher = prefixMatcher
    return this
  }

  override fun stopHere() {}
}
