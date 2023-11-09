package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiNameIdentifierOwner

/**
 * @author Edoardo Luppi
 */
public interface ConventionalCommitScopePsiElement : PsiNameIdentifierOwner {
  /**
   * Returns the scope's value token.
   */
  public fun getValue(): ConventionalCommitScopeValuePsiElement?

  /**
   * Returns if the scope has been completed with a closing parenthesis, or not.
   */
  public fun hasClosingParenthesis(): Boolean
}
