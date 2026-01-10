package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.inspection.quickfix.AddWhitespaceQuickFix
import com.github.lppedd.cc.inspection.quickfix.RemoveRangeQuickFix
import com.github.lppedd.cc.inspection.quickfix.ReplaceRangeQuickFix
import com.github.lppedd.cc.language.psi.ConventionalCommitPsiElementVisitor
import com.github.lppedd.cc.language.psi.ConventionalCommitScopePsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitSubjectPsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitTypePsiElement
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
import com.intellij.codeInspection.ProblemHighlightType.WARNING
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.ui.dsl.builder.Panel

/**
 * @author Edoardo Luppi
 */
internal class CommitFormatInspection : CommitBaseInspection() {
  override fun getDisplayName(): String =
    CCBundle["cc.inspection.nonStdMessage.description"]

  override fun isEnabledByDefault(): Boolean =
    true

  override fun Panel.createOptions(project: Project, disposable: Disposable): Boolean {
    val ui = CommitFormatInspectionOptions()

    row {
      cell(ui.component)
        .onApply { ui.apply(project) }
        .onReset { ui.reset(project) }
        .onIsModified { ui.isModified(project) }
    }

    return false
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val document = holder.file.document
    return if (document != null && isInspectionEnabled(document)) {
      MyVisitor(holder)
    } else {
      super.buildVisitor(holder, isOnTheFly)
    }
  }

  override fun canReformat(project: Project, document: Document): Boolean =
    hasProblems(project, document)

  override fun hasProblems(project: Project, document: Document): Boolean =
    collectProblems(project, document).isNotEmpty()

  override fun reformat(project: Project, document: Document) {
    val documentManager = PsiDocumentManager.getInstance(project)

    for (problem in collectProblems(project, document)) {
      val quickFixes = problem.fixes ?: return

      for (fix in quickFixes) {
        if (fix is CommitBaseQuickFix) {
          fix.applyFix(project, document, problem)
          documentManager.commitDocument(document)
        }
      }
    }
  }

  private fun collectProblems(project: Project, document: Document): List<ProblemDescriptor> {
    if (isInspectionEnabled(document)) {
      val documentManager = PsiDocumentManager.getInstance(project)
      val inspectionManager = InspectionManager.getInstance(project)
      val psiFile = documentManager.getPsiFile(document) ?: return emptyList()
      val problemsHolder = ProblemsHolder(inspectionManager, psiFile, true)
      val visitor = MyRecursiveVisitor(problemsHolder)
      visitor.visitFile(psiFile)
      return problemsHolder.results
    }

    return emptyList()
  }

  private fun isInspectionEnabled(document: Document): Boolean =
    document.getUserData(CommitMessage.DATA_KEY)
      ?.editorField
      ?.editor
      ?.isTemplateActive() == false

  private class MyRecursiveVisitor(holder: ProblemsHolder) : MyVisitor(holder) {
    override fun visitElement(element: PsiElement) {
      element.acceptChildren(this)
    }
  }

  private open class MyVisitor(private val holder: ProblemsHolder) : ConventionalCommitPsiElementVisitor() {
    override fun visitType(element: ConventionalCommitTypePsiElement) {
      val text = element.text
      val textRanges = WHITESPACE_REGEX.findAll(text)
        .map(MatchResult::range)
        .map { TextRange(it.first, it.last + 1) }
        .toList()

      for (range in textRanges) {
        val fixes = if (range.startOffset == 0) {
          arrayOf(RemoveRangeQuickFix(), ConventionalCommitReformatQuickFix)
        } else {
          arrayOf(RemoveRangeQuickFix(false))
        }

        holder.registerProblem(
          element,
          CCBundle["cc.inspection.nonStdMessage.text"],
          WARNING,
          range,
          *fixes,
        )
      }
    }

    override fun visitScope(element: ConventionalCommitScopePsiElement) {
      val scopeValue = element.getValue()?.text ?: ""

      if (scopeValue.isBlank()) {
        if (element.hasClosingParenthesis()) {
          holder.registerProblem(
            element,
            CCBundle["cc.inspection.nonStdMessage.emptyScope"],
            GENERIC_ERROR_OR_WARNING,
            TextRange(0, element.textLength),
            RemoveRangeQuickFix(message = CCBundle["cc.inspection.nonStdMessage.removeScope"]),
            ConventionalCommitReformatQuickFix,
          )
        }

        return
      }

      val valuePsiElement = element.nameIdentifier ?: return
      val (valueStart, valueEnd) = valuePsiElement.textRangeInParent
      val char = CCConfigService.getInstance(holder.project).scopeReplaceChar
      val ranges = WHITESPACE_REGEX.findAll(scopeValue)
        .map(MatchResult::range)
        .map { TextRange(/* Account for ( */ it.first + 1, it.last + 2) }
        .toList()

      for (range in ranges) {
        val fix =
          if (char.isEmpty() || valueStart == range.startOffset || valueEnd == range.endOffset) {
            RemoveRangeQuickFix()
          } else {
            ReplaceRangeQuickFix(char)
          }

        holder.registerProblem(
          element,
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          range,
          fix,
          ConventionalCommitReformatQuickFix,
        )
      }
    }

    override fun visitSubject(element: ConventionalCommitSubjectPsiElement) {
      val value = element.text

      if (value.startsWith("  ")) {
        val nonWsIndex = value.indexOfFirst { !it.isWhitespace() }
        val newEnd = if (nonWsIndex < 0) element.textLength else nonWsIndex
        holder.registerProblem(
          element,
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          TextRange(1, newEnd),
          RemoveRangeQuickFix(),
          ConventionalCommitReformatQuickFix,
        )
      } else if (value.isNotEmpty() && !value.firstIsWhitespace()) {
        holder.registerProblem(
          element,
          CCBundle["cc.inspection.nonStdMessage.text"],
          GENERIC_ERROR_OR_WARNING,
          TextRange(0, 1),
          AddWhitespaceQuickFix(1),
          ConventionalCommitReformatQuickFix,
        )
      }
    }
  }
}
