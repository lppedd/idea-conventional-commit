package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElementVisitor

/**
 * @author Edoardo Luppi
 */
public abstract class ConventionalCommitPsiElementVisitor : PsiElementVisitor() {
  public open fun visitType(element: ConventionalCommitTypePsiElement) {}
  public open fun visitScope(element: ConventionalCommitScopePsiElement) {}
  public open fun visitScopeValue(element: ConventionalCommitScopeValuePsiElement) {}
  public open fun visitSubject(element: ConventionalCommitSubjectPsiElement) {}
  public open fun visitFooter(element: ConventionalCommitFooterPsiElement) {}
  public open fun visitFooterType(element: ConventionalCommitFooterTypePsiElement) {}
  public open fun visitFooterValue(element: ConventionalCommitFooterValuePsiElement) {}
}
