package com.github.lppedd.cc.completion

import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal object CommitLookupElementWeigher : LookupElementWeigher("commitLookupElementWeigher") {
  private const val ourMaxItemsPerProvider = MAX_ITEMS_PER_PROVIDER + 10

  override fun weigh(element: LookupElement): Comparable<*> =
    if (element is CommitLookupElement) {
      // Lower priority = less weight = higher-up in the list
      val priority = element.provider.getPriority()
      element.baseWeight + (priority * ourMaxItemsPerProvider) + element.index
    } else {
      0
    }
}
