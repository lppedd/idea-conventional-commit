package com.github.lppedd.cc.completion.resultset

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class WrapperCompletionResultSet(private var completionResultSet: CompletionResultSet) : ResultSet {
  override fun addElement(lookupElement: LookupElement) {
    completionResultSet.addElement(lookupElement)
  }

  override fun withPrefixMatcher(prefix: String): ResultSet {
    completionResultSet = completionResultSet.withPrefixMatcher(prefix)
    return this
  }

  override fun withPrefixMatcher(prefixMatcher: PrefixMatcher): ResultSet {
    completionResultSet = completionResultSet.withPrefixMatcher(prefixMatcher)
    return this
  }

  override fun stopHere() {
    completionResultSet.stopHere()
  }
}
