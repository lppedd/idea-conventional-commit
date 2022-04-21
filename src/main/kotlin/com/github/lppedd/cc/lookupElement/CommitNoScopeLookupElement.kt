package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.completion.providers.FakeProviderWrapper
import com.github.lppedd.cc.psiElement.CommitFakePsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitNoScopeLookupElement(project: Project) :
    CommitLookupElement(
        -1,
        CC.Tokens.PriorityScope,
        FakeProviderWrapper,
    ) {
  private val psiElement = object : CommitFakePsiElement(project, "No scope") {}

  override fun getPsiElement(): CommitFakePsiElement =
    psiElement

  override fun getLookupString(): String =
    ""

  override fun getDisplayedText(): String =
    "No scope"

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.itemText = getDisplayedText()
      it.isItemTextItalic = true
    }
  }
}
