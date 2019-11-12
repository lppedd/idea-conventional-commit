package com.github.lppedd.cc.lookup

import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
abstract class ConventionalCommitLookupElement : LookupElement() {
  open val weight: Int = 10
  abstract val index: Int;

  override fun isCaseSensitive() = false
  override fun getAutoCompletionPolicy() = AutoCompletionPolicy.NEVER_AUTOCOMPLETE
}
