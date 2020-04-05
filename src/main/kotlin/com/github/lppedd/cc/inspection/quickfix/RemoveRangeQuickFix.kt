package com.github.lppedd.cc.inspection.quickfix

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.inspection.CommitBaseQuickFix
import com.github.lppedd.cc.removeRange
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class RemoveRangeQuickFix(
    override val canReformat: Boolean = true,
    message: String = CCBundle["cc.inspection.nonStdMessage.removeWs"],
) : CommitBaseQuickFix(message) {
  override fun applyFix(project: Project, document: Document, descriptor: ProblemDescriptor) {
    val (start, end) = descriptor.textRangeInElement
    document.removeRange(start, end)
  }
}
