package com.github.lppedd.cc.lookupElement

import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal abstract class CommitLookupElement : LookupElement() {
  open val weight: Int = 10
  abstract val index: Int

  override fun isCaseSensitive() = false
  override fun getAutoCompletionPolicy() = AutoCompletionPolicy.NEVER_AUTOCOMPLETE
}
