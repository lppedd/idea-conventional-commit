package com.github.lppedd.cc.psi

import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitTypePsiElement(
  val commitTypeName: String,
  val commitTypeDescription: String?,
  psiManager: PsiManager
) : CommitFakePsiElement(psiManager) {
  override fun toString() = "CommitType:$commitTypeName"
}
