package com.github.lppedd.cc.psi

import com.github.lppedd.cc.api.CommitTypeProvider.CommitType
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
internal class CommitTypePsiElement(
  val commitType: CommitType,
  psiManager: PsiManager
) : CommitFakePsiElement(psiManager) {
  override fun toString() = "$commitType"
}
