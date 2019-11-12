package com.github.lppedd.cc.lookup

import com.github.lppedd.cc.ConventionalCommitIcons
import com.github.lppedd.cc.psi.CommitScopePsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
open class CommitScopeLookupElement(
  override val index: Int,
  private val psiElement: CommitScopePsiElement
) : ConventionalCommitLookupElement() {
  override val weight = 20
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.run {
      itemText = psiElement.commitScopeName
      icon = ConventionalCommitIcons.SCOPE
    }
  }

  override fun getPsiElement() = psiElement
  override fun getLookupString() = psiElement.commitScopeName
}
