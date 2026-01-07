package com.github.lppedd.cc.configuration.component.tokens

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import javax.swing.Icon
import javax.swing.JList

/**
 * @author Edoardo Luppi
 */
internal class CommitTokenCellRenderer(private val cellIcon: Icon) : ColoredListCellRenderer<String>() {
  override fun customizeCellRenderer(
    list: JList<out String>,
    value: String,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean,
  ) {
    ipad = JBUI.insetsLeft(5)
    icon = cellIcon
    border = JBUI.Borders.empty(1, 3, 2, 3)

    append(value)
    SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, isSelected)
  }
}
