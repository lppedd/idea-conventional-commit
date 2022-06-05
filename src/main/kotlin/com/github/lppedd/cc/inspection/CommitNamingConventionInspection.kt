package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.getLine
import com.github.lppedd.cc.isTemplateActive
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.util.RangeValidator
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiFile

/**
 * Checks if symbols outside the legal ones, specified in the inspection options
 * as regex pattern, are used in the commit type or scope.
 *
 * @author Edoardo Luppi
 * @see CommitNamingConventionInspectionOptions
 */
internal class CommitNamingConventionInspection : CommitBaseInspection() {
  override fun getDisplayName(): String =
    CCBundle["cc.inspection.namingConvention.description"]

  override fun isEnabledByDefault(): Boolean =
    true

  override fun createOptionsConfigurable(): ConfigurableUi<Project> =
    CommitNamingConventionInspectionOptions()

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

    val config = manager.project.service<CCConfigService>()
    val invalidRanges = checkHeader(config, document)
    val problemDescriptors = invalidRanges.map {
      manager.createProblemDescriptor(
          file,
          it,
          CCBundle["cc.inspection.namingConvention.text"],
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          true
      )
    }

    return problemDescriptors.toTypedArray()
  }

  private fun checkHeader(config: CCConfigService, document: Document): List<TextRange> =
    buildList {
      val (type, scope) = CCParser.parseHeader(document.getLine(0))

      if (type is ValidToken) {
        addAll(findInvalidRanges(type, Regex(config.typeNamingPattern)))
      }

      if (scope is ValidToken) {
        addAll(findInvalidRanges(scope, Regex(config.scopeNamingPattern)))
      }
    }

  private fun findInvalidRanges(token: ValidToken, regex: Regex): Set<TextRange> {
    val value = token.value.ifEmpty {
      return emptySet()
    }

    val delta = token.range.startOffset
    val rangeValidator = RangeValidator(delta, delta + value.length)

    regex.findAll(value)
      .map(MatchResult::range)
      .forEach { rangeValidator.markValid(delta + it.first, delta + it.last + 1) }

    return rangeValidator.invalidRanges()
  }
}
