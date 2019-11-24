package com.github.lppedd.cc.completion.weigher

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal abstract class CommitElementWeigher<T : CommitLookupElement>(
  private val ccleClass: Class<T>,
  name: String
) : LookupElementWeigher(name) {
  override fun weigh(element: LookupElement): Comparable<*> =
    if (ccleClass.isAssignableFrom(element::class.java)) {
      val ccle = ccleClass.cast(element)
      ccle.index + ccle.weight
    } else {
      0
    }
}
