package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.WHATS_NEW_EP
import com.github.lppedd.cc.api.WhatsNewProvider
import com.github.lppedd.cc.setFocused
import com.github.lppedd.cc.setName
import com.github.lppedd.cc.ui.NoContentTabbedPaneWrapper
import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper.DialogStyle.COMPACT
import com.intellij.util.ui.JBUI.Borders
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

  init {
    isModal = false
    title = CCBundle["cc.whatsnew.title"]
    setCancelButtonText(CommonBundle.getCloseButtonText())
    setDoNotAskOption(whatsNewPanel)
    init()
  }

  override fun createNorthPanel(): JComponent {
    val tabbedPane = NoContentTabbedPaneWrapper(myDisposable).also {
      it.addChangeListener { _ -> tabSelectedHandlers[it.selectedIndex]?.invoke() }
    }

    WHATS_NEW_EP.extensions
      .asSequence()
      .sortedWith(WhatsNewProviderComparator)
      .filter { it.files.fileDescriptions.isNotEmpty() }
      .forEach { provider ->
        tabSelectedHandlers[tabbedPane.tabCount] = {
          whatsNewPanel.setProvider(provider)
          updateActions()
        }

        tabbedPane.addTab(provider.displayName())
      }

    return tabbedPane.component
  }

  override fun createCenterPanel(): JComponent =
    whatsNewPanel

  override fun createSouthPanel(): JComponent =
    super.createSouthPanel().also {
      it.border = Borders.empty(8, 12)
    }

  override fun createActions(): Array<Action> =
    arrayOf(olderAction, newerAction, cancelAction)

  override fun getStyle(): DialogStyle =
    COMPACT

  override fun getDimensionServiceKey(): String =
    "#com.github.lppedd.cc.WhatsNewDialog"

  override fun getPreferredFocusedComponent(): JComponent? =
    myPreferredFocusedComponent

  private fun updateActions() {
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

  private fun updateActionName(action: AbstractAction, baseName: String, version: String?) {
    if (version == null) {
      action.setName(baseName)
    } else {
      action.setName("$baseName - $version")
    }
  }

  companion object {
    private var sharedDialog: WhatsNewDialog? = null

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
      setFocused()
    }

    override fun actionPerformed(actionEvent: ActionEvent) {
      whatsNewPanel.olderChangelog()
      updateActions()
    }
  }

  private inner class NewerAction : AbstractAction(CCBundle["cc.whatsnew.dialog.newer"]) {
    override fun actionPerformed(actionEvent: ActionEvent) {
      whatsNewPanel.newerChangelog()
      updateActions()
    }
  }

  /**
   * Ensure the core plugin provider's tab is always displayed as first.
   */
  private object WhatsNewProviderComparator : Comparator<WhatsNewProvider> {
    override fun compare(p1: WhatsNewProvider?, p2: WhatsNewProvider?): Int =
      when {
        p1 is DefaultWhatsNewProvider -> -1
        p2 is DefaultWhatsNewProvider -> 1
        else -> 0
      }
  }
}
