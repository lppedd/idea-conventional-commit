package com.github.lppedd.cc.completion.filter

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.emptyCollection
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ReflectionUtil.getField
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.atomic.AtomicBoolean

private val IS_MENU_MODIFIED = AtomicBoolean(false)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class MenuEnhancerLookupListener(private val lookup: LookupImpl) : LookupListener, AnActionListener {
  private var shouldAcceptActionsChanges = true
  private var providers = emptyCollection<CommitTokenProvider>()
  private var actions = emptyCollection<FilterProviderAction>()
  private var messageBus = ApplicationManager.getApplication().messageBus.connect()

  init {
    lookup.addLookupListener(this)
    messageBus.subscribe(AnActionListener.TOPIC, this)
  }

  fun setProviders(providers: Collection<CommitTokenProvider>) {
    this.providers = providers
  }

  override fun uiRefreshed() {
    try {
      val myUi = getField<Any>(lookup.javaClass, lookup, null, "myUi")

      if (myUi == null) {
        IS_MENU_MODIFIED.set(false)
        return
      }

      if (IS_MENU_MODIFIED.get() && lookup.isShown) {
        if (shouldAcceptActionsChanges) {
          actions.forEach(FilterProviderAction::reset)
        }

        return
      }

      IS_MENU_MODIFIED.compareAndSet(false, true)

      val myMenuButton = getField<ActionButton>(myUi.javaClass, myUi, null, "myMenuButton")
      val menuActions = myMenuButton.action as DefaultActionGroup

      actions = providers.map { FilterProviderAction(lookup, it) }
      actions.forEach(menuActions::add)
    } catch (ignored: ReflectiveOperationException) {
      // This should never happen, but in case I can't do anything about it,
      // so I'll just clean-up and let the user continue without applying any change
      cleanUp()
    }
  }

  override fun lookupCanceled(event: LookupEvent) {
    cleanUp()
  }

  override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
    if (action is FilterProviderAction) {
      shouldAcceptActionsChanges = false
    }
  }

  override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
    if (action is FilterProviderAction) {
      shouldAcceptActionsChanges = true
    }
  }

  private fun cleanUp() {
    IS_MENU_MODIFIED.set(false)
    providers = emptyCollection()
    lookup.removeLookupListener(this)
    messageBus.disconnect()
  }
}
