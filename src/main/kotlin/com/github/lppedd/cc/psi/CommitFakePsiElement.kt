package com.github.lppedd.cc.psi

import com.intellij.lang.Language
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.light.LightElement

/**
 * @author Edoardo Luppi
 */
internal abstract class CommitFakePsiElement(psiManager: PsiManager)
  : LightElement(psiManager, Language.ANY)
