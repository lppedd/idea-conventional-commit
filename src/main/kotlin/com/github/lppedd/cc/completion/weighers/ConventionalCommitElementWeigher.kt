package com.github.lppedd.cc.completion.weighers

import com.github.lppedd.cc.lookup.ConventionalCommitLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
open class ConventionalCommitElementWeigher<T : ConventionalCommitLookupElement>(
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
