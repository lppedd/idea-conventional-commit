package com.github.lppedd.cc.inspection.quickfix

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.inspection.ConventionalCommitBaseQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class AddWsQuickFix(private val toAdd: Int) :
    ConventionalCommitBaseQuickFix(CCBundle["cc.inspection.nonStdMessage.addWs"]) {
  override fun doApplyFix(project: Project, document: Document, descriptor: ProblemDescriptor?) {
    descriptor?.let {
      val start = it.textRangeInElement.startOffset
      document.insertString(start, " ".repeat(toAdd))
    }
  }
}