package com.github.lppedd.cc.psi

import com.github.lppedd.cc.api.CommitSubjectProvider.CommitSubject
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectPsiElement(
  val commitSubject: CommitSubject,
  psiManager: PsiManager
) : CommitFakePsiElement(psiManager) {
  override fun toString() = "$commitSubject"
}
