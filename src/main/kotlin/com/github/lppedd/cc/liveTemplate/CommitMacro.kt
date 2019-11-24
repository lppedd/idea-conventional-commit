package com.github.lppedd.cc.liveTemplate

import com.intellij.codeInsight.lookup.LookupArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

abstract class CommitMacro : Macro() {
  override fun calculateResult(
    params: Array<Expression>,
    context: ExpressionContext?
  ): Result? = InvokeActionResult(Runnable { getCommitTokens(context) })

  protected open fun getCommitTokens(context: ExpressionContext?) {
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
        queryProviders(project, lookup)
        isCalculating = false
        showLookup()
        refreshUi(true, true)
        ensureSelectionVisible(true)
      }
    }

    ApplicationManager.getApplication().invokeLater(runnable)
  }

  protected open fun queryProviders(project: Project, lookup: LookupImpl) = Unit
}
