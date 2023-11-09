package com.github.lppedd.cc.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
public abstract class CommitBaseQuickFix : LocalQuickFix {
  final override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    applyFix(project, getDocument(descriptor.psiElement) ?: return, descriptor)
  }

  @Suppress("inapplicable_jvm_name")
  @get:JvmName("canReformat")
  public abstract val canReformat: Boolean
  public abstract fun applyFix(project: Project, document: Document, descriptor: ProblemDescriptor)

  private fun getDocument(element: PsiElement): Document? =
    PsiDocumentManager
      .getInstance(element.project)
      .getDocument(element.containingFile)
}
