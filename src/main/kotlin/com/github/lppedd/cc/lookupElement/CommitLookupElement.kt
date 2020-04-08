package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.completion.providers.ProviderWrapper
import com.github.lppedd.cc.psiElement.CommitFakePsiElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal abstract class CommitLookupElement : LookupElement() {
  abstract val index: Int
  abstract val baseWeight: Int
  abstract val provider: ProviderWrapper

  @Volatile
  var valid: Boolean = true

  abstract override fun getPsiElement(): CommitFakePsiElement

  override fun isValid(): Boolean = valid
  override fun isCaseSensitive() = false
  override fun getAutoCompletionPolicy() = AutoCompletionPolicy.NEVER_AUTOCOMPLETE
}
