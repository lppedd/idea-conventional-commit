package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.APP_NAME
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.KEEP_SELECTED
import com.github.lppedd.cc.emptyCollection
import com.github.lppedd.cc.plus
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.util.ReflectionUtil.getField

/**
 * @author Edoardo Luppi
 */
internal class MenuEnhancerLookupListener(
    private val lookup: LookupImpl,
) : LookupListener, PrefixChangeListener {
  private val commandProcessor = CommandProcessor.getInstance()
  private val config = CCConfigService.getInstance(lookup.project)

  @Volatile private var allActions = emptyCollection<AnAction>()
  @Volatile private var filterActions = emptyCollection<FilterAction>()
  @Volatile private var lastKeptAction: FilterAction? = null
  @Volatile private var menuButton: ActionButton? = null
  @Volatile private var reopenMenu = false

  init {
    lookup.addLookupListener(this)
    lookup.addPrefixChangeListener(this, lookup)
  }

  fun setProviders(providers: Collection<CommitTokenProvider>) {
    lastKeptAction = null
    filterActions = providers.map { FilterAction(this, lookup, it) }
    allActions = SettingsActions(this, lookup) + filterActions.toMutableList()
  }

  fun settingSelected() {
    invokeCompletion()
    reopenMenu = true
  }

  fun filterSelected(filterAction: FilterAction): Boolean =
    if (config.providerFilterType == KEEP_SELECTED) {
      keepOnlySelectedOrReset(filterAction)
      menuButton?.click()
      false
    } else {
      reopenMenu = true
      true
    }

  override fun uiRefreshed() {
    try {
      // After much thoughts and trial-and-errors, keeping the Action list
      // in memory and replacing each Action in the ActionGroup (the popup's menu)
      // each time the UI is refreshed, is the only way to have a decent and consistent behavior.
      // A positive side of this logic is that code is much simpler, and filters' state
      // (filtered or not filtered) is maintained for all the Lookup lifecycle without any effort
      val ui = getField<Any>(lookup.javaClass, lookup, null, "myUi") ?: return
      val menuButton = getField<ActionButton>(ui.javaClass, ui, null, "myMenuButton")
      val menuActions = menuButton.action as DefaultActionGroup

      allActions.forEach {
        menuActions.remove(it)
        menuActions.add(it)
      }

      if (reopenMenu) {
        menuButton.click()
        reopenMenu = false
      }

      this.menuButton = menuButton
    } catch (ignored: ReflectiveOperationException) {
      // This should never happen, but in case I can't do anything about it,
      // so I'll just clean-up and let the user continue without applying any change
      lookup.removeLookupListener(this)
    }
  }

  override fun lookupCanceled(event: LookupEvent) {
    lookup.removeLookupListener(this)
  }

  override fun beforeAppend(ch: Char) {
    lastKeptAction = null
    filterActions.forEach(FilterAction::reset)
    lookup.hideLookup(true)
  }

  override fun beforeTruncate() {
    lastKeptAction = null
    filterActions.forEach(FilterAction::reset)
    lookup.hideLookup(true)
  }

  private fun keepOnlySelectedOrReset(filterAction: FilterAction) {
    if (filterAction === lastKeptAction) {
      lastKeptAction = null
      filterActions.forEach(FilterAction::reset)
    } else {
      lastKeptAction = filterAction
      filterActions.forEach { it.filterItems(it !== filterAction) }
    }

    lookup.resort(false)
  }

  private fun invokeCompletion() {
    val project = lookup.project
    val editor = lookup.editor
    val command = Runnable {
      val invokedExplicitly = ApplicationManager.getApplication().isUnitTestMode
      CodeCompletionHandlerBase
        .createHandler(BASIC, invokedExplicitly, !invokedExplicitly, true)
        .invokeCompletion(project, editor, 1)
    }

    commandProcessor.executeCommand(project, command, "Invoke completion", APP_NAME)
  }
}
