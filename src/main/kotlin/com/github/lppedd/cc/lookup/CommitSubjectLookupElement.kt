package com.github.lppedd.cc.lookup

import com.github.lppedd.cc.ConventionalCommitIcons
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
class CommitSubjectLookupElement(
  override val index: Int,
  private val psiElement: CommitSubjectPsiElement
) : ConventionalCommitLookupElement() {
  override val weight = 10
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.run {
      itemText = psiElement.commitSubject
      icon = ConventionalCommitIcons.DESCRIPTION
    }
  }

  override fun getPsiElement() = psiElement
  override fun getLookupString() = psiElement.commitSubject
}
