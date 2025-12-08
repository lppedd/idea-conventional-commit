package com.github.lppedd.cc.ui

import com.github.lppedd.cc.invokeLaterOnEdt
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.ui.TabbedPaneWrapper
import com.intellij.ui.scale.JBUIScale
import java.awt.Dimension
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.JPanel

/**
 * A tabbed pane that attempts to display the tabs' row only, hiding the tab's component.
 *
 * @author Edoardo Luppi
 */
internal class NoContentTabbedPaneWrapper(disposable: Disposable) : TabbedPaneWrapper(disposable), Disposable {
  init {
    Disposer.register(disposable, this)
  }

  @Synchronized
  fun addTab(title: String) {
    addTab(title, JPanel())
  }

  override fun createTabbedPaneHolder(): TabbedPaneHolder =
    MyTabbedPaneHolder(this)

  override fun dispose() {
    // We are interested only in disposing of the created MyTabbedPaneHolder
  }

  private class MyTabbedPaneHolder(wrapper: NoContentTabbedPaneWrapper)
    : TabbedPaneHolder(wrapper),
      PropertyChangeListener,
      Disposable {
    init {
      Disposer.register(wrapper, this)
      JBUIScale.addUserScaleChangeListener(this)
    }

    override fun getPreferredSize(): Dimension {
      val preferredSize = super.getPreferredSize()
      preferredSize.height = height - tabbedPaneWrapper.selectedComponent.height
      return preferredSize
    }

    override fun propertyChange(event: PropertyChangeEvent) {
      invokeLaterOnEdt {
        @Suppress("UnstableApiUsage")
        IdeEventQueue.getInstance().flushQueue()
        revalidate()
        repaint()
      }
    }

    override fun dispose() {
      JBUIScale.removeUserScaleChangeListener(this)
    }
  }
}
