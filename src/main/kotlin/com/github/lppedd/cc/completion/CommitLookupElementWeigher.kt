package com.github.lppedd.cc.completion

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal object CommitLookupElementWeigher : LookupElementWeigher("commitLookupElementWeigher") {
  private const val ourMaxItemsPerProvider = CC.Provider.MaxItems + 10

  // Lower priority = lower weight = higher-up in the list
  override fun weigh(element: LookupElement): Comparable<*> =
    if (element is CommitLookupElement) {
      val providerPriority = element.provider.getPriority() * ourMaxItemsPerProvider
      element.priority + providerPriority + element.index
    } else {
      0
    }
}
