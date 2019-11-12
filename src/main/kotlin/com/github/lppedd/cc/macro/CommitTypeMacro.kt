package com.github.lppedd.cc.macro

import com.github.lppedd.cc.ConventionalCommitBundle
import com.github.lppedd.cc.lookup.CommitTypeLookupElement
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitTypeMacro : Macro() {
  override fun getName() = NAME
  override fun getPresentableName() = "$NAME()"
  override fun calculateResult(
    params: Array<Expression>,
    context: ExpressionContext?
  ) = null

  override fun calculateLookupItems(
    params: Array<Expression>,
    context: ExpressionContext?
  ) = getTypes(context?.run { PsiManager.getInstance(project) })

  private fun getTypes(psiManager: PsiManager?): Array<LookupElement> {
    return if (psiManager == null) emptyArray()
    else arrayOf(
      CommitTypeLookupElement(1, CommitTypePsiElement("refactor", ConventionalCommitBundle["commit.type.refactor"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("fix", ConventionalCommitBundle["commit.type.fix"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("feat", ConventionalCommitBundle["commit.type.feat"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("build", ConventionalCommitBundle["commit.type.build"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("style", ConventionalCommitBundle["commit.type.style"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("test", ConventionalCommitBundle["commit.type.test"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("docs", ConventionalCommitBundle["commit.type.docs"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("perf", ConventionalCommitBundle["commit.type.perf"], psiManager)),
      CommitTypeLookupElement(1, CommitTypePsiElement("ci", ConventionalCommitBundle["commit.type.ci"], psiManager))
    )
  }

  companion object {
    private const val NAME = "conventionalCommitType"
  }
}
