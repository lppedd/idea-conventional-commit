package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.psiElement.CommitTokenPsiElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal sealed class CommitTokenLookupElement : LookupElement() {
  @Volatile
  private var isVisible: Boolean = true

  fun setVisible(isVisible: Boolean) {
    this.isVisible = isVisible
  }

  override fun isValid() =
    isVisible

  override fun isCaseSensitive() =
    false

  override fun getAutoCompletionPolicy() =
    AutoCompletionPolicy.NEVER_AUTOCOMPLETE

  override fun getAllLookupStrings(): Set<String> =
    setOf(lookupString, getItemText())

  /**
   * In autopopup context (see `CompletionAutoPopupHandler`),
   * avoid hiding the commit type when it matches entirely what the user typed.
   */
  override fun isWorthShowingInAutoPopup() =
    true

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.itemText = getItemText()
    presentation.isTypeIconRightAligned = true

    val tokenPresentation = getToken().getPresentation()
    presentation.isItemTextBold = tokenPresentation.isBold()
    presentation.isItemTextItalic = tokenPresentation.isItalic()
    presentation.isStrikeout = tokenPresentation.isStrikeThrough()

    val foreground = tokenPresentation.getForeground()

    if (foreground != null) {
      presentation.itemTextForeground = foreground
    }

    val isRecentlyUsed = getUserData(LookupElementKey.IsRecent) ?: false
    val type = tokenPresentation.getType()
    val text = if (isRecentlyUsed) {
      if (type.isNullOrBlank()) {
        CCBundle["cc.config.providers.recentlyUsed"]
      } else {
        "$type - ${CCBundle["cc.config.providers.recentlyUsed"]}"
      }
    } else {
      type
    }

    presentation.setTypeText(text, tokenPresentation.getIcon())
  }

  /**
   * The text shown to the user for the completion's item.
   */
  abstract fun getItemText(): String

  /**
   * The commit token associated with this completion element.
   */
  abstract fun getToken(): CommitToken

  abstract override fun getPsiElement(): CommitTokenPsiElement
}
