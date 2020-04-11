package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.KEEP_SELECTED
import com.github.lppedd.cc.emptyCollection
import com.github.lppedd.cc.plus
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.util.ReflectionUtil.getField
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class MenuEnhancerLookupListener(private val lookup: LookupImpl) :
    LookupListener,
    PrefixChangeListener {
  private val config = CCConfigService.getInstance(lookup.project)
  @Volatile private var filterActions = emptyCollection<FilterProviderAction>()
  @Volatile private var allActions = emptyCollection<AnAction>()
  @Volatile private var lastKeptAction: FilterProviderAction? = null

  init {
    lookup.addLookupListener(this)
    lookup.addPrefixChangeListener(this, lookup)
  }

  fun setProviders(providers: Collection<CommitTokenProvider>) {
    filterActions = providers.map { FilterProviderAction(this, lookup, it) }
    allActions = FixedActionGroup(lookup) + filterActions.toMutableList()
    lastKeptAction = null
  }

  fun filterClicked(filterAction: FilterProviderAction): Boolean =
    if (config.providerFilterType == KEEP_SELECTED) {
      keepOnlySelectedOrReset(filterAction)
      false
    } else {
      true
    }

  override fun uiRefreshed() {
    try {
      // After much thoughts and trial-and-errors, keeping the Action list
      // in memory and replacing each Action in the ActionGroup (the popup's menu)
      // each time the UI is refreshed, is the only way to have a decent and consistent behavior.
      // A positive side of this logic is that code is much simpler, and filters' state
      // (filtered or not filtered) is maintained for all the Lookup lifecycle without any effort
      val myUi = getField<Any>(lookup.javaClass, lookup, null, "myUi") ?: return
      val myMenuButton = getField<ActionButton>(myUi.javaClass, myUi, null, "myMenuButton")
      val menuActions = myMenuButton.action as DefaultActionGroup

      allActions.forEach {
        menuActions.remove(it)
        menuActions.add(it)
      }
    } catch (ignored: ReflectiveOperationException) {
      // This should never happen, but in case I can't do anything about it,
      // so I'll just clean-up and let the user continue without applying any change
      lookup.removeLookupListener(this)
    }
  }

  override fun lookupCanceled(event: LookupEvent) {
    lookup.removeLookupListener(this)
  }

  override fun beforeAppend(c: Char) {
    reset()
  }

  override fun beforeTruncate() {
    reset()
  }

  private fun keepOnlySelectedOrReset(filterAction: FilterProviderAction) {
    if (filterAction === lastKeptAction) {
      lastKeptAction = null
      filterActions.forEach(FilterProviderAction::reset)
    } else {
      lastKeptAction = filterAction
      filterActions.forEach { it.doFilter(it !== filterAction) }
    }
  }

  private fun reset() {
    lastKeptAction = null
    filterActions.forEach(FilterProviderAction::reset)
  }
}
