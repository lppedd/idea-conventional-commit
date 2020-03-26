package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.SCOPE_EP
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.doWhileCalculating
import com.github.lppedd.cc.getTemplateState
import com.github.lppedd.cc.invokeLaterOnEdt
import com.github.lppedd.cc.lookupElement.CommitNoScopeLookupElement
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupArranger.DefaultArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.LookupImpl.FocusDegree
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CommitScopeMacro : CommitMacro() {
  override fun getName() = "commitScope"

  override fun getCommitTokens(context: ExpressionContext) {
    val editor = context.editor ?: return
    val templateState = editor.getTemplateState() ?: return
    val commitType = templateState.getVariableValue("TYPE")?.text ?: return
    val project = context.project

    invokeLaterOnEdt {
      val lookup = LookupManager.getInstance(project).createLookup(
        editor,
        LookupElement.EMPTY_ARRAY,
        "",
        DefaultArranger()
      ) as LookupImpl

      lookup.focusDegree = FocusDegree.UNFOCUSED
      lookup.addItem(CommitNoScopeLookupElement(), PrefixMatcher.ALWAYS_TRUE)
      lookup.addLookupListener(object : LookupListener {
        override fun itemSelected(event: LookupEvent) {
          if (event.item is CommitNoScopeLookupElement) {
            templateState.nextTab()
          }
        }
      })
      lookup.doWhileCalculating { queryProviders(project, commitType, lookup) }
      lookup.showLookup()
      lookup.refreshUi(true, true)
      lookup.ensureSelectionVisible(true)
    }
  }

  private fun queryProviders(project: Project, commitType: String, lookup: LookupImpl) {
    SCOPE_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)
      .flatMap { it.getCommitScopes(commitType).asSequence() }
      .map { CommitScopePsiElement(project, it) }
      .mapIndexed(::CommitScopeLookupElement)
      .distinctBy(CommitScopeLookupElement::getLookupString)
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
