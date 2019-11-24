package com.github.lppedd.cc.psi

import com.github.lppedd.cc.api.CommitScopeProvider.CommitScope
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
internal class CommitScopePsiElement(
  val commitScope: CommitScope,
  psiManager: PsiManager
) : CommitFakePsiElement(psiManager) {
  override fun toString() = "$commitScope"
}
