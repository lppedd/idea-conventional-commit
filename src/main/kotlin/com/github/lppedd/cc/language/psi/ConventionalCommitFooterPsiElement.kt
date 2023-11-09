package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
public interface ConventionalCommitFooterPsiElement : PsiElement {
  /**
   * Returns the footer's type token.
   */
  public fun getType(): ConventionalCommitFooterTypePsiElement

  /**
   * Returns the footer's value token, or `null` if not present.
   */
  public fun getValue(): ConventionalCommitFooterValuePsiElement?
}
