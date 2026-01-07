package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.document
import com.github.lppedd.cc.isTemplateActive
import com.github.lppedd.cc.language.psi.ConventionalCommitPsiElementVisitor
import com.github.lppedd.cc.language.psi.ConventionalCommitScopeValuePsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitTypePsiElement
import com.github.lppedd.cc.util.RangeValidator
import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiElementVisitor
import com.intellij.ui.dsl.builder.Panel

/**
 * Checks if symbols outside the legal ones, specified in the inspection options
 * as a regex pattern, are used in the commit type or scope.
 *
 * @author Edoardo Luppi
 * @see CommitNamingConventionInspectionOptions
 */
internal class CommitNamingConventionInspection : CommitBaseInspection() {
  override fun getDisplayName(): String =
    CCBundle["cc.inspection.namingConvention.description"]

  override fun isEnabledByDefault(): Boolean =
    true

  override fun Panel.createOptions(project: Project, disposable: Disposable): Boolean {
    val ui = CommitNamingConventionInspectionOptions()

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

  private fun isInspectionEnabled(document: Document): Boolean =
    document.getUserData(CommitMessage.DATA_KEY)
      ?.editorField
      ?.editor
      ?.isTemplateActive() == false

  private class MyVisitor(private val holder: ProblemsHolder) : ConventionalCommitPsiElementVisitor() {
    override fun visitType(element: ConventionalCommitTypePsiElement) {
      val configService = holder.project.service<CCConfigService>()
      val regex = Regex(configService.typeNamingPattern)

      for (range in findInvalidRanges(element.text, regex)) {
        holder.registerProblem(
          element,
          CCBundle["cc.inspection.namingConvention.text"],
          GENERIC_ERROR_OR_WARNING,
          range,
        )
      }
    }

    override fun visitScopeValue(element: ConventionalCommitScopeValuePsiElement) {
      val configService = holder.project.service<CCConfigService>()
      val regex = Regex(configService.scopeNamingPattern)

      for (range in findInvalidRanges(element.text, regex)) {
        holder.registerProblem(
          element,
          CCBundle["cc.inspection.namingConvention.text"],
          GENERIC_ERROR_OR_WARNING,
          range,
        )
      }
    }

    private fun findInvalidRanges(text: String, regex: Regex): Set<TextRange> {
      if (text.isEmpty()) {
        return emptySet()
      }

      val rangeValidator = RangeValidator(0, text.length)

      regex.findAll(text)
        .map(MatchResult::range)
        .forEach { rangeValidator.markValid(it.first, it.last + 1) }

      return rangeValidator.invalidRanges()
    }
  }
}
