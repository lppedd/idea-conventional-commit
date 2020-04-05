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
internal class ReplaceWsQuickFix(
    private val replaceChar: String,
    override val canReformat: Boolean = true,
) : CommitBaseQuickFix(CCBundle["cc.inspection.nonStdMessage.replaceWs", replaceChar]) {
  override fun applyFix(project: Project, document: Document, descriptor: ProblemDescriptor) {
    val (start, end) = descriptor.textRangeInElement
    document.replaceString(start, end, replaceChar)
  }
}
