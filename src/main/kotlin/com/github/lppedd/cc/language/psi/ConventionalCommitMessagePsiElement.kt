package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
interface ConventionalCommitMessagePsiElement : PsiElement {
  /** Returns the commit type token, or `null` of not present. */
  fun getType(): ConventionalCommitTypePsiElement?

  /** Returns the commit scope token, or `null` of not present. */
  fun getScope(): ConventionalCommitScopePsiElement?

  /** Returns the commit subject token, or `null` of not present. */
  fun getSubject(): ConventionalCommitSubjectPsiElement?

  /** Returns the commit body token, or `null` of not present. */
  fun getBody(): ConventionalCommitBodyPsiElement?

  /** Returns all the commit footers tokens. */
  fun getFooters(): Array<out ConventionalCommitFooterPsiElement>
}
