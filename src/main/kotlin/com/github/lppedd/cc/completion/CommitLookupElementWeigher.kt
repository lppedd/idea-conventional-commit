package com.github.lppedd.cc.completion

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal object CommitLookupElementWeigher : LookupElementWeigher("commitLookupElementWeigher") {
  // Lower priority = lower weight = higher-up in the list
  override fun weigh(element: LookupElement): Comparable<*> =
    if (element is CommitLookupElement) {
      element.priority + element.provider.getPriority() * (CC.Provider.MaxItems + 10) + element.index
    } else {
      0
    }
}
