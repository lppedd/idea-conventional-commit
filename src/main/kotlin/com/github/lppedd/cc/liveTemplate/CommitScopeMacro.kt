package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.lookupElement.CommitNoScopeLookupElement
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.psi.CommitScopePsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupArranger.DefaultArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
internal class CommitScopeMacro : CommitMacro() {
  override fun getName() = "commitScope"
  override fun getPresentableName() = "commitScope()"
  override fun getCommitTokens(context: ExpressionContext?) {
    val project = (context ?: return).project
    val editor = context.editor

    val runnable = Runnable {
      val lookup = LookupManager.getInstance(project).createLookup(
        editor ?: return@Runnable,
        LookupElement.EMPTY_ARRAY,
        "",
        DefaultArranger()
      ) as LookupImpl

      val templateState = TemplateManagerImpl.getTemplateState(context.editor ?: return@Runnable)
      val commitType = (templateState?.getVariableValue("TYPE") ?: return@Runnable).text

      lookup.focusDegree = LookupImpl.FocusDegree.UNFOCUSED
      lookup.addItem(CommitNoScopeLookupElement(), PrefixMatcher.ALWAYS_TRUE)
      lookup.addLookupListener(object : LookupListener {
        override fun itemSelected(event: LookupEvent) {
          if (event.item is CommitNoScopeLookupElement) {
            templateState.nextTab()
          }
        }
      })

      with(lookup) {
        isCalculating = true
        queryProviders(project, commitType, lookup)
        isCalculating = false
        showLookup()
        refreshUi(true, true)
        ensureSelectionVisible(true)
      }
    }

    ApplicationManager.getApplication().invokeLater(runnable)
  }

  private fun queryProviders(project: Project, commitType: String, lookup: LookupImpl) {
    val psiManager = PsiManager.getInstance(project)
    CommitScopeProvider.EP_NAME.getExtensions(project)
      .flatMap { it.getCommitScopes(commitType) }
      .map { CommitScopePsiElement(it, psiManager) }
      .mapIndexed(::CommitScopeLookupElement)
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
