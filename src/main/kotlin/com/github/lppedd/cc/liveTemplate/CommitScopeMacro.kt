package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.completion.providers.ScopeCompletionProvider
import com.github.lppedd.cc.completion.resultset.LookupResultSet
import com.github.lppedd.cc.doWhileCalculating
import com.github.lppedd.cc.getTemplateState
import com.github.lppedd.cc.invokeLaterOnEdt
import com.github.lppedd.cc.lookupElement.CommitNoScopeLookupElement
import com.github.lppedd.cc.parser.CommitContext.ScopeCommitContext
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupArranger.DefaultArranger
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.LookupImpl.FocusDegree
import com.intellij.codeInsight.template.ExpressionContext

/**
 * @author Edoardo Luppi
 */
private class CommitScopeMacro : CommitMacro() {
  override fun getName() =
    "commitScope"

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
      lookup.addItem(CommitNoScopeLookupElement, PrefixMatcher.ALWAYS_TRUE)
      lookup.addLookupListener(object : LookupListener {
        override fun itemSelected(event: LookupEvent) {
          if (event.item === CommitNoScopeLookupElement) {
            templateState.nextTab()
          }
        }
      })
      lookup.doWhileCalculating {
        val commitContext = ScopeCommitContext(commitType, "")
        val provider = ScopeCompletionProvider(project, commitContext)
        provider.complete(LookupResultSet(lookup), false)
      }
      lookup.showLookup()
      lookup.refreshUi(true, true)
      lookup.ensureSelectionVisible(true)
    }
  }
}
