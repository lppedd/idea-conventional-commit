package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.completion.Priority
import com.github.lppedd.cc.completion.providers.ProviderWrapper
import com.github.lppedd.cc.psiElement.CommitFakePsiElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal abstract class CommitLookupElement(
    val index: Int,
    val priority: Priority,
    val provider: ProviderWrapper,
) : LookupElement() {
  @Volatile
  var valid: Boolean = true

  override fun isValid() =
    valid

  override fun isCaseSensitive() =
    false

  override fun getAutoCompletionPolicy() =
    AutoCompletionPolicy.NEVER_AUTOCOMPLETE

  override fun getAllLookupStrings(): Set<String> =
    setOf(lookupString, getDisplayedText())

  /**
   * In autopopup context (see `CompletionAutoPopupHandler`),
   * avoid hiding the commit type when it matches entirely what the user typed.
   */
  override fun isWorthShowingInAutoPopup() =
    true

  abstract override fun getPsiElement(): CommitFakePsiElement

  /**
   * The text shown to the user for the completion's item.
   */
  abstract fun getDisplayedText(): String
}
