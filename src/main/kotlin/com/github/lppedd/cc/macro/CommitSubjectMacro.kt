package com.github.lppedd.cc.macro

import com.github.lppedd.cc.lookup.CommitSubjectLookupElement
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.psi.PsiManager
import com.intellij.util.containers.toArray

/**
 * @author Edoardo Luppi
 */
class CommitSubjectMacro : Macro() {
  override fun getName() = NAME
  override fun getPresentableName() = "$NAME()"
  override fun calculateResult(
    params: Array<Expression>,
    context: ExpressionContext?
  ) = null

  override fun calculateLookupItems(
    params: Array<Expression>,
    context: ExpressionContext?
  ) = getRecentVcsMessages(context!!.project, 20)

  @Suppress("SameParameterValue")
  fun getRecentVcsMessages(project: Project, limit: Int): Array<LookupElement> {
    val psiManager = PsiManager.getInstance(project)
    val recentMessages = VcsConfiguration.getInstance(project).recentMessages.reversed()
    return recentMessages
      .take(limit)
      .map { v -> v.replaceFirst("(^(build|fix|refactor|chore|feat|docs)).*:".toRegex(), "") }
      .map { obj -> obj.trim { it <= ' ' } }
      .mapIndexed { index, description ->
        CommitSubjectLookupElement(
          index,
          CommitSubjectPsiElement(description, psiManager)
        )
      }
      .toArray(emptyArray())
  }

  companion object {
    private const val NAME = "conventionalCommitDescription"
  }
}
