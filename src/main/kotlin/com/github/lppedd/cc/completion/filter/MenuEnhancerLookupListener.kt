package com.github.lppedd.cc.completion.filter

import com.github.lppedd.cc.api.CommitTokenProvider
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.util.ReflectionUtil.getField
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.atomic.AtomicBoolean

private val IS_MENU_MODIFIED = AtomicBoolean(false)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class MenuEnhancerLookupListener(private val lookup: LookupImpl) : LookupListener {
  private var providers: Collection<CommitTokenProvider> = emptyList()

  init {
    lookup.addLookupListener(this)
  }

  fun setProviders(providers: Collection<CommitTokenProvider>) {
    this.providers = providers
  }

  override fun uiRefreshed() {
    try {
      if (IS_MENU_MODIFIED.get() && lookup.isShown) {
        return
      }

      val myUi = getField<Any>(lookup.javaClass, lookup, null, "myUi") ?: return

      IS_MENU_MODIFIED.compareAndSet(false, true)

      val myMenuButton = getField<ActionButton>(myUi.javaClass, myUi, null, "myMenuButton")
      val menuActions = myMenuButton.action as DefaultActionGroup

      providers
        .map { FilterProviderAction(lookup, it) }
        .forEach(menuActions::add)
    } catch (ignored: ReflectiveOperationException) {
      // This should never happen, but in case I can't do anything about it,
      // so I'll just clean-up and let the user continue without applying any change
      cleanUp()
    }
  }

  override fun lookupCanceled(event: LookupEvent) {
    cleanUp()
  }

  private fun cleanUp() {
    IS_MENU_MODIFIED.set(false)
    lookup.removeLookupListener(this)
    providers = emptyList()
  }
}
