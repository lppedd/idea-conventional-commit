package com.github.lppedd.cc.completion

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.menu.FilterAction
import com.github.lppedd.cc.completion.menu.SettingsActions
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.KEEP_SELECTED
import com.github.lppedd.cc.keyPressAndRelease
import com.github.lppedd.cc.plus
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupFocusDegree
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.actionSystem.ex.ActionPopupMenuListener
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.ReflectionUtil.getField
import java.awt.Robot
import java.awt.event.KeyEvent

/**
 * Known issues:
 * - the menu retains focus after opening, see [IDEA-254427](https://youtrack.jetbrains.com/issue/IDEA-254427)
 *
 * @author Edoardo Luppi
 */
internal class LookupEnhancer(
    private val lookup: LookupImpl,
) : LookupListener, PrefixChangeListener, AnActionListener {
  private companion object {
    private const val SHOW_GROUP_IN_POPUP = "actionSystem.toolbar.show.group.in.popup"

    private val menuActionClass = Class.forName("com.intellij.codeInsight.lookup.impl.LookupUi\$MenuAction")
    private val logger = logger<LookupEnhancer>()
    private val robot = Robot()
  }

  private val commandProcessor = CommandProcessor.getInstance()
  private val actionManager = ActionManagerEx.getInstanceEx()
  private val config = lookup.project.service<CCConfigService>()

  @Volatile private var lookupPopupMenuListenerDisposable: Disposable? = null
  @Volatile private var allActions = emptyList<AnAction>()
  @Volatile private var filterActions = emptyList<FilterAction>()
  @Volatile private var lastKeptAction: FilterAction? = null
  @Volatile private var menuButton: ActionButton? = null
  @Volatile private var reopenMenu = false
  @Volatile private var closeMenu = false

  init {
    lookup.addLookupListener(this)
    lookup.addPrefixChangeListener(this, lookup)
    lookup.project.messageBus.connect(lookup).subscribe(AnActionListener.TOPIC, this)
  }

  fun setProviders(providers: Collection<CommitTokenProvider>) {
    lastKeptAction = null
    closeMenu = false
    filterActions = providers.map { FilterAction(this, lookup, it) }
    allActions = SettingsActions(this, lookup) + filterActions.toMutableList()
  }

  fun settingChanged() {
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

  override fun lookupShown(event: LookupEvent) {
    try {
      // Setting the lookup focus degree to "semi-focused" means the top lookup item
      // matching the prefix is preselected and ready to be completed by non-contextual keys
      // TODO: maybe consider using a CompletionPreselectionBehaviourProvider.
      //   However the provider evaluation happens only if "...depends.on.context"
      //   registry key is set to "true".
      //   Also if the user checked the "Insert selected suggestion by... context-dependent keys"
      //   option, the FOCUSED degree is used while we want SEMI_FOCUSED
      lookup.lookupFocusDegree = LookupFocusDegree.SEMI_FOCUSED
    } catch (e: Exception) {
      logger.error("Couldn't override the lookup focus degree", e)
    }
  }

  override fun uiRefreshed() {
    try {
      // After many thoughts and trial-and-errors, keeping the Action list
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
    } catch (_: ReflectiveOperationException) {
      // This should never happen, but in case I can't do anything about it,
      // so I'll just clean up and let the user continue without applying any change
      lookup.removeLookupListener(this)
    }
  }

  override fun lookupCanceled(event: LookupEvent) {
    lookup.removeLookupListener(this)
    closeMenu = false
  }

  override fun beforeAppend(ch: Char) {
    lastKeptAction = null
    filterActions.forEach { it.filterItems(false) }

    if (closeMenu) {
      closeMenu = false
      robot.keyPressAndRelease(KeyEvent.VK_ESCAPE)
    }
  }

  override fun beforeTruncate() {
    lastKeptAction = null
    filterActions.forEach { it.filterItems(false) }
  }

  @Suppress("override_deprecation")
  override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
    if (action.javaClass == menuActionClass && !Registry.`is`(SHOW_GROUP_IN_POPUP, false)) {
      var disposable = lookupPopupMenuListenerDisposable
      disposable?.dispose()
      disposable = Disposer.newDisposable(lookup, "LookupEnhancer popup menu listener")
      actionManager.addActionPopupMenuListener(LookupPopupMenuListener(disposable), disposable)
      lookupPopupMenuListenerDisposable = disposable
      closeMenu = true
    }
  }

  private fun keepOnlySelectedOrReset(filterAction: FilterAction) {
    if (filterAction === lastKeptAction) {
      lastKeptAction = null
      filterActions.forEach { it.filterItems(false) }
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

    commandProcessor.executeCommand(project, command, "Invoke completion", CC.AppName)
  }

  private inner class LookupPopupMenuListener(private val disposable: Disposable) : ActionPopupMenuListener {
    override fun actionPopupMenuReleased(menu: ActionPopupMenu) {
      closeMenu = false
      Disposer.dispose(disposable)
    }
  }
}
