package com.github.lppedd.cc.configuration.component.tokens

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.Borders
import com.intellij.util.ui.UIUtil
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
    if (!isSelected && index % 2 == 0) {
      background = UIUtil.getDecoratedRowColor()
    }

    ipad = JBUI.insetsLeft(5)
    icon = cellIcon
    border = Borders.empty(1, 3, 2, 3)

    append(value)
    SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, isSelected)
  }
}
