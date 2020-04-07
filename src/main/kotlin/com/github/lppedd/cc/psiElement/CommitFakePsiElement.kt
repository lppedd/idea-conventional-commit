package com.github.lppedd.cc.psiElement

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.FakePsiElement

/**
 * @author Edoardo Luppi
 */
internal abstract class CommitFakePsiElement(
    private val project: Project,
    val ownedBy: String = "",
) : FakePsiElement() {
  private val psiManager by lazy { PsiManager.getInstance(project) }

  override fun getContainingFile() = null
  override fun getParent() = null
  override fun getProject() = project
  override fun getManager() = psiManager
  override fun isValid() = true
  override fun isPhysical() = false
  override fun isWritable() = false
}
