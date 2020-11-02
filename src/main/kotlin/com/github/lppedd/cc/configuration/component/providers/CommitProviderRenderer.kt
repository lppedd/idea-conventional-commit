package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.wrap
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderRenderer : DefaultTableCellRenderer() {
  companion object {
    private val APP = ApplicationInfo.getInstance()
    private val ICON_HW =
      if ("${APP.majorVersion}.${APP.minorVersion}" == "2019.2") 16f
      else 32f
  }

  override fun getTableCellRendererComponent(
      table: JTable,
      value: Any,
      isSelected: Boolean,
      hasFocus: Boolean,
      row: Int,
      column: Int,
  ): Component {
    super.getTableCellRendererComponent(table, value, isSelected, false, row, column)
    val paddingBorder = JBUI.Borders.empty(1, 3, 2, 3)
    val compoundBorder = border.wrap(paddingBorder)
    border = compoundBorder
    return this
  }

  override fun setValue(value: Any) {
    if (value is CommitTokenProvider) {
      val (name, icon) = value.getPresentation()
      text = name
      setIcon(getIcon(icon))
    } else {
      text = null
      icon = null
    }
  }

  private fun getIcon(icon: Icon): Icon {
    val scaledHW = JBUIScale.scale(ICON_HW)
    return if (icon.iconHeight <= scaledHW && icon.iconWidth <= scaledHW) {
      icon
    } else {
      // The icon doesn't match the size prerequisite.
      // Thus we display a generic one
      CCIcons.Provider.Unknown
    }
  }
}
