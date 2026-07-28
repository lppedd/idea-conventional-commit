package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.document
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.InspectionToolRegistrar
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.removeUserData
import com.intellij.openapi.vcs.VcsBundle
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiElementVisitor

/**
 * A port of the now deleted class from the IntelliJ Community codebase.
 *
 * @author Edoardo Luppi
 */
internal class CommitMessageSpellCheckingInspection : CommitBaseInspection() {
  private companion object {
    private val logger = logger<CommitMessageSpellCheckingInspection>()
    private val ourSpellCheckingInspection by lazy {
      val tools = InspectionToolRegistrar.getInstance().createTools()
      val toolWrapper = tools.find { it.shortName == "SpellCheckingInspection" }
      when (toolWrapper) {
        null -> {
          logger.info("Could not find default spell checking inspection")
          null
        }
        !is LocalInspectionToolWrapper -> {
          logger.info("Found spell checking wrapper is not local $toolWrapper")
          null
        }
        else -> toolWrapper.tool
      }
    }
  }

  override fun getDisplayName(): String =
    VcsBundle.message("inspection.CommitMessageSpellCheckingInspection.display.name")

  override fun isEnabledByDefault(): Boolean =
    true

  override fun getDefaultLevel(): HighlightDisplayLevel =
    HighlightDisplayLevel.find("TYPO") ?: HighlightDisplayLevel.WARNING

  override fun buildVisitor(
    holder: ProblemsHolder,
    isOnTheFly: Boolean,
    session: LocalInspectionToolSession,
  ): PsiElementVisitor {
    val tool = ourSpellCheckingInspection ?: return PsiElementVisitor.EMPTY_VISITOR
    val document = session.file.document ?: return PsiElementVisitor.EMPTY_VISITOR
    val commitMessage = document.removeUserData(CommitMessage.DATA_KEY)

    try {
      return tool.buildVisitor(holder, isOnTheFly, session)
    } finally {
      document.putUserData(CommitMessage.DATA_KEY, commitMessage)
    }
  }
}
