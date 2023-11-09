package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.WhatsNewProvider
import com.github.lppedd.cc.api.WhatsNewProviderService
import com.github.lppedd.cc.ui.CCDialogWrapper
import com.github.lppedd.cc.ui.NoContentTabbedPaneWrapper
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper.DialogStyle.COMPACT
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tabs.impl.TabLabel
import com.intellij.util.ui.JBUI
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

/**
 * A dialog which display changelogs using [WhatsNewProvider]s,
 * each rendered as a separate tab.
 *
 * @author Edoardo Luppi
 */
internal class WhatsNewDialog(project: Project) : CCDialogWrapper(project) {
  private val whatsNewPanel = WhatsNewPanel()
  private val olderAction = OlderAction()
  private val newerAction = NewerAction()
  private val tabSelectedHandlers = mutableMapOf<Int, () -> Unit>()

  private val providers = application.service<WhatsNewProviderService>()
    .getWhatsNewProviders()
    .asSequence()
    .sortedWith(WhatsNewProviderComparator)
    .filter { it.getPages().isNotEmpty() }
    .toList()

  private lateinit var tabbedPane: NoContentTabbedPaneWrapper

  init {
    isModal = false
    title = CCBundle["cc.whatsnew.title"]
    setCancelButtonText(CCBundle["cc.whatsnew.dialog.close"])
    setDoNotAskOption(whatsNewPanel)
    init()
  }

  override fun createNorthPanel(): JComponent? {
    if (providers.isEmpty()) {
      cancelAction.setFocused()
      return null
    }

    olderAction.setFocused()

    tabbedPane = NoContentTabbedPaneWrapper(myDisposable)
    tabbedPane.addChangeListener {
      tabSelectedHandlers[tabbedPane.selectedIndex]?.invoke()
    }

    providers.forEach { provider ->
      tabSelectedHandlers[tabbedPane.tabCount] = {
        whatsNewPanel.setProvider(provider)
        updateComponents()
      }

      tabbedPane.addTab(provider.getDisplayName())
    }

    return tabbedPane.component
  }

  override fun createCenterPanel(): JComponent =
    if (providers.isEmpty()) {
      WhatsNewEmptyPanel()
    } else {
      whatsNewPanel
    }

  override fun createSouthPanel(): JComponent =
    super.createSouthPanel().also {
      it.border = JBUI.Borders.empty(8, 12)
    }

  override fun createActions(): Array<Action> =
    arrayOf(olderAction, newerAction, cancelAction)

  override fun getStyle(): DialogStyle =
    COMPACT

  override fun getPreferredFocusedComponent(): JComponent? =
    myPreferredFocusedComponent

  private fun updateComponents() {
    updateTabTitle()

    val currentVersion = whatsNewPanel.currentVersion()
    val hasNewer = whatsNewPanel.hasNewer()
    val hasOlder = whatsNewPanel.hasOlder()
    newerAction.isEnabled = hasNewer
    olderAction.isEnabled = hasOlder

    val newerVersion = if (hasNewer) whatsNewPanel.newerVersion() else currentVersion
    updateActionName(newerAction, CCBundle["cc.whatsnew.dialog.newer"], newerVersion)

    val olderVersion = if (hasOlder) whatsNewPanel.olderVersion() else currentVersion
    updateActionName(olderAction, CCBundle["cc.whatsnew.dialog.older"], olderVersion)
  }

  private fun updateTabTitle() {
    val currentVersion = whatsNewPanel.currentVersion()
    val selectedTabIndex = tabbedPane.selectedIndex

    if (currentVersion == null || selectedTabIndex < 0) {
      return
    }

    val text = SimpleColoredText().also {
      it.append(tabbedPane.getTitleAt(selectedTabIndex), SimpleTextAttributes.REGULAR_ATTRIBUTES)
      it.append(" (", SimpleTextAttributes.REGULAR_ATTRIBUTES)
      it.append(currentVersion, SimpleTextAttributes.REGULAR_ATTRIBUTES)
      it.append(")", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    val tabLabel = tabbedPane.getTabComponentAt(selectedTabIndex)

    if (tabLabel is TabLabel) {
      tabLabel.setText(text)
    }
  }

  private fun updateActionName(action: AbstractAction, baseName: String, version: String?) {
    if (version == null) {
      action.setName(baseName)
    } else {
      action.setName("$baseName ($version)")
    }
  }

  companion object {
    const val PROPERTY_SHOW = "com.github.lppedd.cc.whatsnew.show"

    private var sharedDialog: WhatsNewDialog? = null

    @Synchronized
    fun showForProject(project: Project) {
      var dialog = sharedDialog

      if (dialog != null && dialog.isVisible) {
        dialog.dispose()
      }

      dialog = WhatsNewDialog(project)
      sharedDialog = dialog
      dialog.show()
    }
  }

  private inner class OlderAction : AbstractAction(CCBundle["cc.whatsnew.dialog.older"]) {
    init {
      isEnabled = false
    }

    override fun actionPerformed(actionEvent: ActionEvent) {
      whatsNewPanel.olderChangelog()
      updateComponents()
    }
  }

  private inner class NewerAction : AbstractAction(CCBundle["cc.whatsnew.dialog.newer"]) {
    init {
      isEnabled = false
    }

    override fun actionPerformed(actionEvent: ActionEvent) {
      whatsNewPanel.newerChangelog()
      updateComponents()
    }
  }

  /**
   * Ensure the core plugin provider's tab is always displayed as first.
   */
  private object WhatsNewProviderComparator : Comparator<WhatsNewProvider> {
    override fun compare(p1: WhatsNewProvider?, p2: WhatsNewProvider?): Int =
      when {
        p1 is InternalWhatsNewProvider -> -1
        p2 is InternalWhatsNewProvider -> 1
        else -> 0
      }
  }
}
