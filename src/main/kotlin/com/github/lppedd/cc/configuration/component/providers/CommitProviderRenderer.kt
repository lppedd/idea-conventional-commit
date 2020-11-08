package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.api.CommitTokenProvider
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import javax.swing.Icon
import javax.swing.JTable

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderRenderer : ColoredTableCellRenderer() {
  companion object {
    private val ICON_HW = ApplicationInfo.getInstance().let {
      if ("${it.majorVersion}.${it.minorVersion}" == "2019.2") 16f
      else 32f
    }
  }

  override fun isTransparentIconBackground(): Boolean =
    true

  override fun customizeCellRenderer(
      table: JTable,
      value: Any?,
      isSelected: Boolean,
      hasFocus: Boolean,
      row: Int,
      column: Int,
  ) {
    border = JBUI.Borders.empty()
    ipad = JBUI.insets(4, 6)
    setValue(value)
    SpeedSearchUtil.applySpeedSearchHighlighting(table, this, true, isSelected)
  }

  private fun setValue(value: Any?) {
    if (value is CommitTokenProvider) {
      val (name, icon) = value.getPresentation()
      setIcon(getIcon(icon))
      append(name)
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
