package com.github.lppedd.cc.completion.resultset

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal open class DelegateResultSet(private var resultSet: CompletionResultSet) : ResultSet {
  override fun addElement(lookupElement: LookupElement) {
    resultSet.addElement(lookupElement)
  }

  override fun withPrefixMatcher(prefix: String): ResultSet {
    resultSet = resultSet.withPrefixMatcher(prefix)
    return this
  }

  override fun withPrefixMatcher(prefixMatcher: PrefixMatcher): ResultSet {
    resultSet = resultSet.withPrefixMatcher(prefixMatcher)
    return this
  }

  override fun stopHere() {
    resultSet.stopHere()
  }
}
