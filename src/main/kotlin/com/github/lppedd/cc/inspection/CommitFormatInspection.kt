package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.inspection.quickfix.RemoveWsQuickFix
import com.github.lppedd.cc.inspection.quickfix.ReplaceWsQuickFix
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
import com.intellij.openapi.editor.Document
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

/**
 * @author Edoardo Luppi
 */
internal class CommitFormatInspection : ConventionalCommitBaseInspection() {
  override fun getDisplayName(): String =
    CCBundle["cc.inspection.nonStandardMessage"]

  override fun createOptionsConfigurable(): ConfigurableUi<Project> =
    CommitFormatInspectionOptions()

  override fun checkFile(
      file: PsiFile,
      document: Document,
      manager: InspectionManager,
      isOnTheFly: Boolean,
  ): Array<ProblemDescriptor> {
    if (document.lineCount > 0) {
      return checkHeader(file, document, manager).toTypedArray()
    }

    return emptyArray()
  }

  private fun checkHeader(
      psiFile: PsiFile,
      document: Document,
      manager: InspectionManager,
  ): List<ProblemDescriptor> {
    val (_, scope, _, _, subject) = CCParser.parseHeader(document.getLine(0))
    val problems = mutableListOf<ProblemDescriptor>()

    if (scope is ValidToken) {
      problems += handleScope(scope, manager, psiFile)
    }

    if (subject is ValidToken && subject.value.startsWith("  ")) {
      problems += handleSubject(subject, manager, psiFile)
    }

    return problems
  }

  private fun handleSubject(
      subject: ValidToken,
      manager: InspectionManager,
      psiFile: PsiFile,
  ): ProblemDescriptor {
    val nonWsIndex = subject.value.indexOfFirst { !it.isWhitespace() }
    val start = subject.range.first
    val end = if (nonWsIndex < 0) subject.range.last else start + nonWsIndex
    val range = TextRange(start, end)
    return manager.createProblemDescriptor(
      psiFile,
      range,
      CCBundle["cc.inspection.nonStandardMessage.ws"],
      GENERIC_ERROR_OR_WARNING,
      true,
      RemoveWsQuickFix(1),
      ConventionalCommitReformatQuickFix
    )
  }

  private fun handleScope(
      scope: ValidToken,
      manager: InspectionManager,
      psiFile: PsiFile,
  ): List<ProblemDescriptor> {
    val (start, end) = scope.range
    return WHITESPACE_REGEX.findAll(scope.value)
      .map(MatchResult::range)
      .map { TextRange(start + it.first, start + it.last + 1) }
      .map {
        val quickFix =
          if (it.startOffset == start || it.endOffset == end) {
            RemoveWsQuickFix(0)
          } else {
            ReplaceWsQuickFix(CCConfigService.getInstance(
              manager.project).scopeReplaceChar)
          }

        manager.createProblemDescriptor(
          psiFile,
          it,
          CCBundle["cc.inspection.nonStandardMessage.ws"],
          GENERIC_ERROR_OR_WARNING,
          true,
          quickFix,
          ConventionalCommitReformatQuickFix
        )
      }.toList()
  }

  override fun canReformat(project: Project, document: Document): Boolean =
    hasProblems(project, document)

  override fun reformat(project: Project, document: Document) {
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
    val problemsToQuickFixes =
      checkFile(psiFile, document, InspectionManager.getInstance(project), false)
        .map { it to it.fixes?.filterIsInstance<ConventionalCommitBaseQuickFix>() }
        .asReversed()

    for ((problemDescriptor, quickFixes) in problemsToQuickFixes) {
      quickFixes?.asReversed()?.forEach {
        it.doApplyFix(project, document, problemDescriptor)
      }
    }
  }
}
