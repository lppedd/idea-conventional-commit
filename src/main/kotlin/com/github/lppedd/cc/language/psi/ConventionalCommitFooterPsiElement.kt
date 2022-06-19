package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
interface ConventionalCommitFooterPsiElement : PsiElement {
  /** Returns the actual text of the footer's type. */
  fun getFooterType(): String

  /** Returns the actual text of the footer's value, or `null` if not present. */
  fun getFooterValue(): String?
}
