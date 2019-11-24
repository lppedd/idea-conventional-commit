package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.api.CommitTokenProvider
import com.intellij.openapi.application.ApplicationInfo
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderRenderer<T : CommitTokenProvider> : DefaultTableCellRenderer() {
  companion object {
    private val APP = ApplicationInfo.getInstance()
    private val ICON_HW = getIconSizeForVersion()

    private fun getIconSizeForVersion(): Int {
      return when (APP.minorVersion) {
        "2"  -> 16
        "3"  -> 32
        else -> 32
      }
    }
  }

  override fun getTableCellRendererComponent(
    table: JTable,
    value: Any,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int
  ): Component? {
    super.getTableCellRendererComponent(table, value, isSelected, false, row, column)
    val paddingBorder = BorderFactory.createEmptyBorder(1, 3, 2, 3)
    val compoundBorder = BorderFactory.createCompoundBorder(border, paddingBorder)
    border = compoundBorder
    return this
  }

  override fun setValue(value: Any) {
    if (value is CommitTokenProvider) {
      icon = getIcon(value)
      text = getText(value)
    } else {
      icon = null
      text = null
    }
  }

  private fun getText(provider: CommitTokenProvider) = provider.getPresentationName()
  private fun getIcon(configuration: CommitTokenProvider): Icon {
    val icon = configuration.getPresentationIcon()
    return if (icon.iconHeight <= ICON_HW && icon.iconWidth <= ICON_HW) {
      icon
    } else {
      // The icon doesn't match the size prerequisite.
      // Thus we display a generic one
      CCIcons.UNKNOWN_PROVIDER
    }
  }
}
