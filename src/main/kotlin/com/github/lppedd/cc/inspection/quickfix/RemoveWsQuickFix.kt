package com.github.lppedd.cc.inspection.quickfix

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.inspection.ConventionalCommitBaseQuickFix
import com.github.lppedd.cc.removeRange
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class RemoveWsQuickFix(private val toKeep: Int) :
    ConventionalCommitBaseQuickFix(CCBundle["cc.inspection.nonStdMessage.removeWs"]) {
  override fun doApplyFix(project: Project, document: Document, descriptor: ProblemDescriptor?) {
    descriptor?.let {
      val (start, end) = it.textRangeInElement
      document.removeRange(start + toKeep, end)
    }
  }
}
