package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
public interface ConventionalCommitMessagePsiElement : PsiElement {
  /**
   * Returns the commit type token, or `null` of not present.
   */
  public fun getType(): ConventionalCommitTypePsiElement?

  /**
   * Returns the commit scope token, or `null` of not present.
   */
  public fun getScope(): ConventionalCommitScopePsiElement?

  /**
   * Returns the commit subject token, or `null` of not present.
   */
  public fun getSubject(): ConventionalCommitSubjectPsiElement?

  /**
   * Returns the commit body token, or `null` of not present.
   */
  public fun getBody(): ConventionalCommitBodyPsiElement?

  /**
   * Returns all the commit footers tokens.
   */
  public fun getFooters(): Array<out ConventionalCommitFooterPsiElement>
}
