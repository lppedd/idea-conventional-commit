package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitScope
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.psiElement.CommitTokenPsiElement
import com.github.lppedd.cc.psiElement.NoScopeCommitPsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitNoScopeLookupElement(
  private val psiElement: NoScopeCommitPsiElement,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    NoScopeCommitScope

  override fun getPsiElement(): CommitTokenPsiElement =
    psiElement

  override fun getLookupString(): String =
    ""

  override fun getItemText(): String =
    psiElement.presentableText

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.itemText = getItemText()
    presentation.isItemTextItalic = true
  }

  private object NoScopeCommitScope : CommitScope {
    override fun getValue(): String =
      ""

    override fun getText(): String =
      CCBundle["cc.completion.noScope"]
  }
}
