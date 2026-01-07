package com.github.lppedd.cc.inspection.quickfix

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.inspection.CommitBaseQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class ReplaceRangeQuickFix(
  private val replaceChar: String,
  override val canReformat: Boolean = true,
) : CommitBaseQuickFix() {
  override fun getFamilyName(): String =
    CCBundle["cc.inspection.nonStdMessage.replaceWs", replaceChar]

  override fun applyFix(project: Project, document: Document, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val (start) = element.textRange
    val (startInElement, endInElement) = descriptor.textRangeInElement
    document.replaceString(start + startInElement, start + endInElement, replaceChar)
  }
}
