package com.github.lppedd.cc.completion

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

// This is just a big number
private const val MAX_ELEMENTS_PER_PROVIDER = 1_000

/**
 * @author Edoardo Luppi
 */
internal object CommitLookupElementWeigher : LookupElementWeigher("commitLookupElementWeigher") {
  override fun weigh(element: LookupElement): Comparable<*> =
    if (element is CommitLookupElement) {
      // Lower priority = less weight = higher-up in the list
      val priority = element.provider.getPriority()
      element.baseWeight + (priority * MAX_ELEMENTS_PER_PROVIDER) + element.index
    } else {
      0
    }
}
