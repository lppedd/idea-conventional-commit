package com.github.lppedd.cc.inspection.quickfix

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.inspection.ConventionalCommitBaseQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class ReplaceWsQuickFix(private val replaceChar: String) :
    ConventionalCommitBaseQuickFix(CCBundle["cc.inspection.nonStdMessage.replaceWs", replaceChar]) {
  override fun doApplyFix(project: Project, document: Document, descriptor: ProblemDescriptor?) {
    descriptor?.let {
      val (start, end) = it.textRangeInElement
      document.replaceString(start, end, replaceChar)
    }
  }
}
