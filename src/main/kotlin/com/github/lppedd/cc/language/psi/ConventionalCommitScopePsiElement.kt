package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiNameIdentifierOwner

/**
 * @author Edoardo Luppi
 */
interface ConventionalCommitScopePsiElement : PsiNameIdentifierOwner {
  /** Returns the scope's value token. */
  fun getValue(): ConventionalCommitScopeValuePsiElement?

  /** Returns if the scope has been completed with a closing parenthesis, or not. */
  fun hasClosingParenthesis(): Boolean
}
