package com.github.lppedd.cc.psi

import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitSubjectPsiElement(
  val commitSubject: String,
  psiManager: PsiManager
) : CommitFakePsiElement(psiManager) {
  override fun toString() = "CommitSubject:$commitSubject"
}
