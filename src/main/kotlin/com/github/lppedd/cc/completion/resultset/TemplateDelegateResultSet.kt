package com.github.lppedd.cc.completion.resultset

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal class TemplateDelegateResultSet(resultSet: CompletionResultSet) : DelegateResultSet(resultSet) {
  override fun addElement(lookupElement: LookupElement) {
    super.addElement(decorateIfPossible(lookupElement))
  }

  private fun decorateIfPossible(lookupElement: LookupElement): LookupElement =
    if (lookupElement is CommitLookupElement) {
      TemplateElementDecorator(lookupElement)
    } else {
      lookupElement
    }
}
