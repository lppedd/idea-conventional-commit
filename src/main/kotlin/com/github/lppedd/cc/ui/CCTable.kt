package com.github.lppedd.cc.ui

import com.github.lppedd.cc.CCUI
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.table.TableModel

/**
 * @author Edoardo Luppi
 */
internal open class CCTable : JBTable {
  constructor() : super()
  constructor(model: TableModel) : super(model)

  init {
    background = CCUI.ListBackgroundColor

    // See https://jetbrains.design/intellij/controls/table/#interaction
    // It seems this behavior isn't implemented at Platform level,
    // so we need to do it ourselves
    addFocusListener(object : FocusListener {
      override fun focusGained(e: FocusEvent?) {
        updateColorsForFocusState(isFocused = true)
      }

      override fun focusLost(event: FocusEvent?) {
        updateColorsForFocusState(isFocused = false)
      }

      private fun updateColorsForFocusState(isFocused: Boolean) {
        setSelectionForeground(UIUtil.getTableSelectionForeground(isFocused))
        setSelectionBackground(UIUtil.getTableSelectionBackground(isFocused))
      }
    })
  }
}
