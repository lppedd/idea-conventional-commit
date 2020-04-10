package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.emptyCollection
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
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
  @Volatile private var actions = emptyCollection<FilterProviderAction>()

  init {
    lookup.addLookupListener(this)
    lookup.addPrefixChangeListener(this, lookup)
  }

  fun setProviders(providers: Collection<CommitTokenProvider>) {
    actions = providers.map { FilterProviderAction(lookup, it) }
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

      actions.forEach {
        menuActions.remove(it)
        menuActions.add(it)
      }
    } catch (ignored: ReflectiveOperationException) {
      // This should never happen, but in case I can't do anything about it,
      // so I'll just clean-up and let the user continue without applying any change
      cleanUp()
    }
  }

  override fun lookupCanceled(event: LookupEvent) {
    cleanUp()
  }

  override fun beforeAppend(c: Char) {
    actions.forEach(FilterProviderAction::reset)
  }

  override fun beforeTruncate() {
    actions.forEach(FilterProviderAction::reset)
  }

  private fun cleanUp() {
    lookup.removeLookupListener(this)
  }
}
