package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiNameIdentifierOwner

/**
 * @author Edoardo Luppi
 */
interface ConventionalCommitScopePsiElement : PsiNameIdentifierOwner {
  /** Returns if the scope has been completed with a closing parenthesis, or not. */
  fun hasClosingParenthesis(): Boolean

  /** Returns the actual text of the scope token. */
  fun getValue(): String
}
