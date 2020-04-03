package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.configuration.component.AbstractTableEditableModel

internal typealias CoAuthors = Collection<String>

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsTableModel(coAuthors: CoAuthors) : AbstractTableEditableModel() {
  private val coAuthorRows = coAuthors.map(::CoAuthorRow).toMutableList()

  val coAuthors: CoAuthors
    get() = coAuthorRows.map(CoAuthorRow::text)

  val selectedCoAuthors: CoAuthors
    get() = coAuthorRows.filter(CoAuthorRow::isSelected).map(CoAuthorRow::text)

  override fun getRowCount(): Int =
    coAuthorRows.size

  override fun getColumnCount(): Int =
    2

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
    coAuthorRows[rowIndex][columnIndex]

  override fun setValueAt(value: Any, rowIndex: Int, columnIndex: Int) {
    coAuthorRows[rowIndex][columnIndex] = value
    fireTableCellUpdated(rowIndex, columnIndex)
  }

  override fun addRow() {
    coAuthorRows.add(0, CoAuthorRow(isSelected = true))
    fireTableRowsInserted(0, 0)
  }

  override fun removeRow(idx: Int) {
    coAuthorRows.removeAt(idx)
    fireTableRowsDeleted(idx, idx)
  }

  fun removeRowIfEmpty(rowIndex: Int) {
    if (coAuthorRows[rowIndex].text.isBlank()) {
      removeRow(rowIndex)
    }
  }

  override fun getColumnClass(columnIndex: Int): Class<*>? =
    if (coAuthorRows.isNotEmpty()) getValueAt(0, columnIndex)?.javaClass
    else null

  override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
    true

  override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean =
    true

  override fun exchangeRows(oldIndex: Int, newIndex: Int) {
    if (newIndex < coAuthorRows.size) {
      coAuthorRows.add(newIndex, coAuthorRows.removeAt(oldIndex))
      fireTableRowsUpdated(oldIndex.coerceAtMost(newIndex), oldIndex.coerceAtLeast(newIndex))
    }
  }
}

private data class CoAuthorRow(var text: String = "", var isSelected: Boolean = false) {
  operator fun get(columnIndex: Int): Any? =
    when (columnIndex) {
      0 -> isSelected
      1 -> text
      else -> throw IllegalArgumentException("Column index $columnIndex does not exist")
    }

  operator fun set(columnIndex: Int, value: Any) {
    when (columnIndex) {
      0 -> isSelected = value as Boolean
      1 -> text = value as String
      else -> throw IllegalArgumentException("Column index $columnIndex does not exist")
    }
  }
}
