package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElementVisitor

/**
 * @author Edoardo Luppi
 */
abstract class ConventionalCommitPsiElementVisitor : PsiElementVisitor() {
  open fun visitType(element: ConventionalCommitTypePsiElement) {}

  open fun visitScope(element: ConventionalCommitScopePsiElement) {}

  open fun visitScopeValue(element: ConventionalCommitScopeValuePsiElement) {}

  open fun visitSubject(element: ConventionalCommitSubjectPsiElement) {}

  open fun visitFooter(element: ConventionalCommitFooterPsiElement) {}

  open fun visitFooterType(element: ConventionalCommitFooterTypePsiElement) {}

  open fun visitFooterValue(element: ConventionalCommitFooterValuePsiElement) {}
}
