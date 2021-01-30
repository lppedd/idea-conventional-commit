package com.github.lppedd.cc.inspection.quickfix

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.inspection.CommitBaseQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class AddWhitespaceQuickFix(
    private val toAdd: Int,
    override val canReformat: Boolean = true,
) : CommitBaseQuickFix() {
  override fun getFamilyName(): String =
    CCBundle["cc.inspection.nonStdMessage.addWs"]

  override fun applyFix(project: Project, document: Document, descriptor: ProblemDescriptor) {
    val start = descriptor.textRangeInElement.startOffset
    document.insertString(start, " ".repeat(toAdd))
  }
}
