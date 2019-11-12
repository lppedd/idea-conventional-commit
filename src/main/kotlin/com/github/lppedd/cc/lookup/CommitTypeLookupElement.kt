package com.github.lppedd.cc.lookup

import com.github.lppedd.cc.ConventionalCommitIcons
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
open class CommitTypeLookupElement(
  override val index: Int,
  private val psiElement: CommitTypePsiElement
) : ConventionalCommitLookupElement() {
  override val weight = 30
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.run {
      itemText = psiElement.commitTypeName
      icon = ConventionalCommitIcons.TYPE
    }
  }

  override fun getPsiElement() = psiElement
  override fun getLookupString() = psiElement.commitTypeName
}
