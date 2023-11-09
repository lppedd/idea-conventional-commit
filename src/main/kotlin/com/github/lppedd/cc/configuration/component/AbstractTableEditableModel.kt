package com.github.lppedd.cc.configuration.component

import com.intellij.util.ui.EditableModel
import javax.swing.table.AbstractTableModel

/**
 * @author Edoardo Luppi
 */
internal abstract class AbstractTableEditableModel : AbstractTableModel(), EditableModel {
  override fun addRow() {}
  override fun removeRow(idx: Int) {}
  override fun exchangeRows(oldIndex: Int, newIndex: Int) {}
  override fun canExchangeRows(oldIndex: Int, newIndex: Int) =
    false
}
