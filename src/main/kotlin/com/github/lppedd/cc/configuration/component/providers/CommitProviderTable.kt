package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.ui.CCTable
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import javax.swing.Icon
import javax.swing.JTable

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderTable<T : CommitTokenProvider> : CCTable() {
  private companion object {
    private const val COLUMN_PROVIDER = 0

    private val iconSize = ApplicationInfo.getInstance().let {
      if ("${it.majorVersion}.${it.minorVersion}" == "2019.2") {
        16f
      } else {
        32f
      }
    }
  }

  private val model = CommitProviderModel<T>()
  private var latest = emptyList<T>()

  init {
    setModel(model)
    emptyText.text = CCBundle["cc.config.providers.empty"]

    setTableHeader(null)
    setShowGrid(false)
    getColumnModel().getColumn(COLUMN_PROVIDER).also {
      it.minWidth = 100
      it.cellRenderer = CommitProviderRenderer()
    }
  }

  var providers: List<T>
    get() = model.getProviders()
    set(providers) {
      latest = providers.toList()
      model.setProviders(providers.toMutableList())
    }

  fun isModified(): Boolean {
    val current = model.getProviders()
    return latest.size != current.size || latest != current
  }

  fun reset() {
    model.setProviders(latest.toMutableList())
  }

  private class CommitProviderRenderer : ColoredTableCellRenderer() {
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
      setValue(value, isSelected && hasFocus)
      SpeedSearchUtil.applySpeedSearchHighlighting(table, this, true, isSelected)
    }

    private fun setValue(value: Any?, useDarkIcon: Boolean) {
      if (value is CommitTokenProvider) {
        val (name, icon) = value.getPresentation()
        val fixedIcon = getIconOrUnknown(icon)
        setIcon(if (useDarkIcon) IconLoader.getDarkIcon(fixedIcon, true) else fixedIcon)
        append(name)
      }
    }

    private fun getIconOrUnknown(icon: Icon): Icon {
      val scaledHW = JBUIScale.scale(iconSize)
      return if (icon.iconHeight <= scaledHW && icon.iconWidth <= scaledHW) {
        icon
      } else {
        // The icon doesn't match the size prerequisite.
        // Thus we display a generic one
        CCIcons.Provider.Unknown
      }
    }
  }
}
