package com.github.lppedd.cc.psi

import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitScopePsiElement(
  val commitScopeName: String,
  psiManager: PsiManager
) : CommitFakePsiElement(psiManager) {
  override fun toString() = "CommitScope:$commitScopeName"
}
