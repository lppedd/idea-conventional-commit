package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.doWhileCalculating
import com.github.lppedd.cc.invokeLaterOnEdt
import com.intellij.codeInsight.lookup.LookupArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.LookupImpl.FocusDegree
import com.intellij.codeInsight.template.*
import com.intellij.openapi.project.Project

internal abstract class CommitMacro : Macro() {
  override fun getPresentableName() = "$name()"

  override fun calculateResult(
      params: Array<Expression>,
      context: ExpressionContext?,
  ): Result? =
    if (context != null) {
      InvokeActionResult(Runnable { getCommitTokens(context) })
    } else {
      null
    }

  protected open fun getCommitTokens(context: ExpressionContext) {
    val editor = context.editor ?: return
    val project = context.project

    invokeLaterOnEdt {
      val lookup = LookupManager.getInstance(project).createLookup(
        editor,
        LookupElement.EMPTY_ARRAY,
        "",
        LookupArranger.DefaultArranger()
      ) as LookupImpl

      lookup.focusDegree = FocusDegree.UNFOCUSED
      lookup.doWhileCalculating { queryProviders(project, lookup) }
      lookup.showLookup()
      lookup.refreshUi(true, true)
      lookup.ensureSelectionVisible(true)
    }
  }

  protected open fun queryProviders(project: Project, lookup: LookupImpl) = Unit
}
