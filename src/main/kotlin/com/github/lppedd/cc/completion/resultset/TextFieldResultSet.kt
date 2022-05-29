package com.github.lppedd.cc.completion.resultset

import com.github.lppedd.cc.lookupElement.CommitTokenLookupElement
import com.github.lppedd.cc.lookupElement.TextFieldLookupElementDecorator
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal class TextFieldResultSet(private var resultSet: CompletionResultSet) : ResultSet {
  override fun addElement(lookupElement: LookupElement) =
    resultSet.addElement(decorateIfNeeded(lookupElement))

  override fun withPrefixMatcher(prefix: String): ResultSet {
    resultSet = resultSet.withPrefixMatcher(prefix)
    return this
  }

  override fun withPrefixMatcher(prefixMatcher: PrefixMatcher): ResultSet {
    resultSet = resultSet.withPrefixMatcher(prefixMatcher)
    return this
  }

  override fun stopHere() =
    resultSet.stopHere()

  private fun decorateIfNeeded(lookupElement: LookupElement): LookupElement =
    if (lookupElement is CommitTokenLookupElement) {
      TextFieldLookupElementDecorator(lookupElement)
    } else {
      lookupElement
    }
}
