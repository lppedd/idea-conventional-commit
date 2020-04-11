package com.github.lppedd.cc.completion.resultset

import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal interface ResultSet {
  fun addElement(lookupElement: LookupElement)
  fun withPrefixMatcher(prefix: String): ResultSet
  fun withPrefixMatcher(prefixMatcher: PrefixMatcher): ResultSet
  fun stopHere()
}
