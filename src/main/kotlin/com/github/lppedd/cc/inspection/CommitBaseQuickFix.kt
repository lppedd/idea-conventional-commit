package com.github.lppedd.cc.inspection

import com.intellij.codeInspection.LocalQuickFixBase
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
abstract class CommitBaseQuickFix(name: String) : LocalQuickFixBase(name) {
  final override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    doApplyFix(project, getDocument(descriptor.psiElement) ?: return, descriptor)
  }

  abstract fun doApplyFix(project: Project, document: Document, descriptor: ProblemDescriptor)

  private fun getDocument(element: PsiElement): Document? =
    PsiDocumentManager
      .getInstance(element.project)
      .getDocument(element.containingFile)
}
