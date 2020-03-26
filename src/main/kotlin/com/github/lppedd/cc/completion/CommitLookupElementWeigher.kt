package com.github.lppedd.cc.completion

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal object CommitLookupElementWeigher : LookupElementWeigher("commitLookupElementWeigher") {
  override fun weigh(element: LookupElement): Comparable<*> =
    if (element is CommitLookupElement) {
      element.index + element.weight
    } else {
      0
    }
}
