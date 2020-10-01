package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.inspection.quickfix.AddWsQuickFix
import com.github.lppedd.cc.inspection.quickfix.RemoveRangeQuickFix
import com.github.lppedd.cc.inspection.quickfix.ReplaceRangeQuickFix
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
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
internal class CommitFormatInspection : CommitBaseInspection() {
  override fun getDisplayName(): String =
    CCBundle["cc.inspection.nonStdMessage.description"]

  override fun isEnabledByDefault(): Boolean =
    true

  override fun createOptionsConfigurable(): ConfigurableUi<Project> =
    CommitFormatInspectionOptions()

  override fun checkFile(
      file: PsiFile,
      document: Document,
      manager: InspectionManager,
      isOnTheFly: Boolean,
  ): Array<ProblemDescriptor> {
    if (!isTemplateActive() && document.lineCount > 0) {
      return checkHeader(file, document, manager).toTypedArray()
    }

    return emptyArray()
  }

  private fun checkHeader(
      psiFile: PsiFile,
      document: Document,
      manager: InspectionManager,
  ): List<ProblemDescriptor> {
    val firstLine = document.getLine(0)
    val (type, scope, _, _, subject) = CCParser.parseHeader(firstLine)
    val problems = mutableListOf<ProblemDescriptor>()

    if (type is ValidToken) {
      handleType(type, manager, psiFile, firstLine).let { problems += it }
    }

    if (scope is ValidToken) {
      problems += handleScope(scope, manager, psiFile, firstLine)
    }

    if (subject is ValidToken) {
      handleSubject(subject, manager, psiFile)?.let { problems += it }
    }

    return problems
  }

  private fun handleType(
      type: ValidToken,
      manager: InspectionManager,
      psiFile: PsiFile,
      firstLine: CharSequence,
  ): List<ProblemDescriptor> {
    val start = type.range.startOffset

    if (start == 0) {
      return emptyList()
    }

    return WHITESPACE_REGEX.findAll(firstLine.take(start))
      .map(MatchResult::range)
      .map { TextRange(it.first, it.last + 1) }
      .map {
        val quickFixes: Array<LocalQuickFix> = if (it.startOffset == 0) {
          arrayOf(RemoveRangeQuickFix(), ConventionalCommitReformatQuickFix)
        } else {
          arrayOf(RemoveRangeQuickFix(false))
        }

        manager.createProblemDescriptor(
          psiFile,
          it,
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          true,
          *quickFixes
        )
      }.toList()
  }

  private fun handleScope(
      scope: ValidToken,
      manager: InspectionManager,
      psiFile: PsiFile,
      firstLine: CharSequence,
  ): List<ProblemDescriptor> {
    val (start, end) = scope.range
    val value = scope.value

    if (value.isBlank() && ')' == firstLine.getOrNull(end)) {
      return listOf(manager.createProblemDescriptor(
        psiFile,
        TextRange(start - 1, end + 1),
        CCBundle["cc.inspection.nonStdMessage.emptyScope"],
        GENERIC_ERROR_OR_WARNING,
        true,
        RemoveRangeQuickFix(message = CCBundle["cc.inspection.nonStdMessage.removeScope"]),
        ConventionalCommitReformatQuickFix
      ))
    }

    return WHITESPACE_REGEX.findAll(value)
      .map(MatchResult::range)
      .map { TextRange(start + it.first, start + it.last + 1) }
      .map {
        val quickFix =
          if (it.startOffset == start || it.endOffset == end) {
            RemoveRangeQuickFix()
          } else {
            val config = CCConfigService.getInstance(manager.project)
            ReplaceRangeQuickFix(config.scopeReplaceChar)
          }

        manager.createProblemDescriptor(
          psiFile,
          it,
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          true,
          quickFix,
          ConventionalCommitReformatQuickFix
        )
      }.toList()
  }

  private fun handleSubject(
      subject: ValidToken,
      manager: InspectionManager,
      psiFile: PsiFile,
  ): ProblemDescriptor? {
    val value = subject.value
    val (start, end) = subject.range
    return when {
      value.startsWith("  ") -> {
        val nonWsIndex = value.indexOfFirst { !it.isWhitespace() }
        val newEnd = if (nonWsIndex < 0) end else start + nonWsIndex
        manager.createProblemDescriptor(
          psiFile,
          TextRange(start + 1, newEnd),
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          true,
          RemoveRangeQuickFix(),
          ConventionalCommitReformatQuickFix
        )
      }
      value.isNotEmpty() && !value.firstIsWhitespace() -> {
        manager.createProblemDescriptor(
          psiFile,
          TextRange(start, start + 1),
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          true,
          AddWsQuickFix(1),
          ConventionalCommitReformatQuickFix
        )
      }
      else -> null
    }
  }

  override fun canReformat(project: Project, document: Document): Boolean =
    hasProblems(project, document)

  override fun reformat(project: Project, document: Document) {
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
    val problemsToQuickFixes =
      checkFile(psiFile, document, InspectionManager.getInstance(project), false)
        .map {
          it to it.fixes
            ?.filterIsInstance<CommitBaseQuickFix>()
            ?.filter(CommitBaseQuickFix::canReformat)
        }.asReversed()

    for ((problemDescriptor, quickFixes) in problemsToQuickFixes) {
      quickFixes?.asReversed()?.forEach {
        it.applyFix(project, document, problemDescriptor)
      }
    }
  }
}
