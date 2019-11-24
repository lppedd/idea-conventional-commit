package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitTypeMacro : CommitMacro() {
  override fun getName() = "commitType"
  override fun getPresentableName() = "commitType()"
  override fun getCommitTokens(context: ExpressionContext?) {
    val project = (context ?: return).project
    val editor = context.editor

    val runnable = Runnable {
      val lookup = LookupManager.getInstance(project).createLookup(
        editor ?: return@Runnable,
        LookupElement.EMPTY_ARRAY,
        "",
        LookupArranger.DefaultArranger()
      ) as LookupImpl

      lookup.focusDegree = LookupImpl.FocusDegree.UNFOCUSED

      with(lookup) {
        isCalculating = true
        queryExtensions(project, lookup)
        isCalculating = false
        showLookup()
        refreshUi(true, true)
        ensureSelectionVisible(true)
      }
    }

    ApplicationManager.getApplication().invokeLater(runnable)
  }

  private fun queryExtensions(project: Project, lookup: LookupImpl) {
    val psiManager = PsiManager.getInstance(project)
    CommitTypeProvider.EP_NAME.getExtensions(project)
      .flatMap { it.getCommitTypes("") }
      .map { CommitTypePsiElement(it, psiManager) }
      .mapIndexed { i, psi -> CommitTypeLookupElement(i, psi) }
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
