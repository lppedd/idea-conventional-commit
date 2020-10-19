package com.github.lppedd.cc.ui

import com.intellij.openapi.Disposable
import com.intellij.ui.TabbedPaneWrapper
import java.awt.Dimension
import javax.swing.JPanel

/**
 * A tabbed pane which attempt to display the tabs' row only, hiding the tab's component.
 *
 * @author Edoardo Luppi
 */
class NoContentTabbedPaneWrapper(disposable: Disposable) : TabbedPaneWrapper(disposable) {
  @Synchronized
  fun addTab(title: String) {
    addTab(title, JPanel())
  }

  override fun createTabbedPaneHolder(): TabbedPaneHolder =
    MyTabbedPaneHolder(this)

  private class MyTabbedPaneHolder(wrapper: TabbedPaneWrapper) : TabbedPaneHolder(wrapper) {
    override fun getPreferredSize(): Dimension {
      val preferredSize = super.getPreferredSize()
      preferredSize.height = height - tabbedPaneWrapper.selectedComponent.height
      return preferredSize
    }
  }
}
