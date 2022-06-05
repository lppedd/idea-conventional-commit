package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.inspection.quickfix.AddWhitespaceQuickFix
import com.github.lppedd.cc.inspection.quickfix.RemoveRangeQuickFix
import com.github.lppedd.cc.inspection.quickfix.ReplaceRangeQuickFix
import com.github.lppedd.cc.language.psi.ConventionalCommitMessagePsiElement
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPlainText
import com.intellij.psi.util.PsiTreeUtil

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
    val isTemplateActive =
      document.getUserData(CommitMessage.DATA_KEY)
        ?.editorField
        ?.editor
        ?.isTemplateActive() == true

    if (isTemplateActive || document.lineCount == 0) {
      return emptyArray()
    }

    val psiElement = PsiTreeUtil.getChildOfAnyType(
        file,
        ConventionalCommitMessagePsiElement::class.java, PsiPlainText::class.java,
    ) ?: return emptyArray()

    return checkHeader(psiElement, document, manager).toTypedArray()
  }

  private fun checkHeader(
      psiElement: PsiElement,
      document: Document,
      manager: InspectionManager,
  ): List<ProblemDescriptor> {
    val firstLine = document.getLine(0)
    val (type, scope, _, _, subject) = CCParser.parseHeader(firstLine)
    val problems = mutableListOf<ProblemDescriptor>()

    if (type is ValidToken) {
      problems += handleType(type, manager, psiElement, firstLine)
    }

    if (scope is ValidToken) {
      problems += handleScope(scope, manager, psiElement, firstLine)
    }

    if (subject is ValidToken) {
      handleSubject(subject, manager, psiElement)?.let {
        problems += it
      }
    }

    return problems
  }

  private fun handleType(
      type: ValidToken,
      manager: InspectionManager,
      psiElement: PsiElement,
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
        // An explicit type is required for being compatible with IDE releases beyond 2019.
        // In 2019.* BaseCommitMessageQuickFix extends LocalQuickFixBase, while starting
        // from 2020.2 BaseCommitMessageQuickFix extends LocalQuickFix.
        // With an implicit type ConventionalCommitReformatQuickFix would always be cast
        // to LocalQuickFixBase, generating an exception
        val quickFixes: Array<LocalQuickFix> = if (it.startOffset == 0) {
          arrayOf(RemoveRangeQuickFix(), ConventionalCommitReformatQuickFix)
        } else {
          arrayOf(RemoveRangeQuickFix(false))
        }

        manager.createProblemDescriptor(
            psiElement,
            it,
            CCBundle["cc.inspection.nonStdMessage.text"],
            GENERIC_ERROR_OR_WARNING,
            true,
            *quickFixes,
        )
      }.toList()
  }

  private fun handleScope(
      scope: ValidToken,
      manager: InspectionManager,
      psiElement: PsiElement,
      firstLine: CharSequence,
  ): List<ProblemDescriptor> {
    val (start, end) = scope.range
    val value = scope.value

    if (value.isBlank() && ')' == firstLine.getOrNull(end)) {
      return listOf(manager.createProblemDescriptor(
          psiElement,
          TextRange(start - 1, end + 1),
          CCBundle["cc.inspection.nonStdMessage.emptyScope"],
          GENERIC_ERROR_OR_WARNING,
          true,
          RemoveRangeQuickFix(message = CCBundle["cc.inspection.nonStdMessage.removeScope"]),
          ConventionalCommitReformatQuickFix,
      ))
    }

    return WHITESPACE_REGEX.findAll(value)
      .map(MatchResult::range)
      .map { TextRange(start + it.first, start + it.last + 1) }
      .map {
        val scopeReplaceChar = manager.project.service<CCConfigService>().scopeReplaceChar
        val quickFix =
          if (it.startOffset == start || it.endOffset == end || scopeReplaceChar.isEmpty()) {
            RemoveRangeQuickFix()
          } else {
            ReplaceRangeQuickFix(scopeReplaceChar)
          }

        manager.createProblemDescriptor(
            psiElement,
            it,
            CCBundle["cc.inspection.nonStdMessage.text"],
            GENERIC_ERROR_OR_WARNING,
            true,
            quickFix,
            ConventionalCommitReformatQuickFix,
        )
      }.toList()
  }

  private fun handleSubject(
      subject: ValidToken,
      manager: InspectionManager,
      psiElement: PsiElement,
  ): ProblemDescriptor? {
    val value = subject.value
    val (start, end) = subject.range
    return when {
      value.startsWith("  ") -> {
        val nonWsIndex = value.indexOfFirst { !it.isWhitespace() }
        val newEnd = if (nonWsIndex < 0) end else start + nonWsIndex
        manager.createProblemDescriptor(
            psiElement,
            TextRange(start + 1, newEnd),
            CCBundle["cc.inspection.nonStdMessage.text"],
            GENERIC_ERROR_OR_WARNING,
            true,
            RemoveRangeQuickFix(),
            ConventionalCommitReformatQuickFix,
        )
      }
      value.isNotEmpty() && !value.firstIsWhitespace() -> {
        manager.createProblemDescriptor(
            psiElement,
            TextRange(start, start + 1),
            CCBundle["cc.inspection.nonStdMessage.text"],
            GENERIC_ERROR_OR_WARNING,
            true,
            AddWhitespaceQuickFix(1),
            ConventionalCommitReformatQuickFix,
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
