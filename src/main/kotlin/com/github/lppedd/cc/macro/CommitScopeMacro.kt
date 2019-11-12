package com.github.lppedd.cc.macro

import com.github.lppedd.cc.lookup.NoScopeLookupElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupArranger.DefaultArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
class CommitScopeMacro : Macro() {
  override fun getName() = NAME
  override fun getPresentableName() = "$NAME()"
  override fun calculateResult(
    params: Array<Expression>,
    context: ExpressionContext?
  ): Result? =
    InvokeActionResult(Runnable { invokeCompletion(context) })

  private fun invokeCompletion(context: ExpressionContext?) {
    val project = (context ?: return).project
    val editor = context.editor
    val lookupManager = LookupManager.getInstance(project)

    val runnable = Runnable {
      val lookup = lookupManager.createLookup(
        editor ?: return@Runnable,
        LookupElement.EMPTY_ARRAY,
        "",
        DefaultArranger()
      ) as LookupImpl

      lookup.focusDegree = LookupImpl.FocusDegree.UNFOCUSED
      lookup.addItem(NoScopeLookupElement(), PrefixMatcher.ALWAYS_TRUE)
      lookup.addLookupListener(object : LookupListener {
        override fun itemSelected(event: LookupEvent) {
          if (event.item is NoScopeLookupElement) {
            val templateState = TemplateManagerImpl.getTemplateState(context.editor ?: return)
            templateState?.nextTab()
          }
        }
      })

      with(lookup) {
        isCalculating = true
        findNgModules(project, lookup)
        isCalculating = false
        showLookup()
        refreshUi(true, true)
        ensureSelectionVisible(true)
      }
    }

    ApplicationManager.getApplication().invokeLater(runnable)
  }

  private fun findNgModules(project: Project, lookup: LookupImpl) {

  }

  companion object {
    private const val NAME = "conventionalCommitScope"
  }
}
