package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.configuration.component.AbstractTableEditableModel

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderModel<T : CommitTokenProvider> : AbstractTableEditableModel() {
  private var internalProviders = mutableListOf<T>()

  var providers: List<T>
    get() = internalProviders
    set(value) {
      internalProviders = value.toMutableList()
      fireTableDataChanged()
    }

  override fun getColumnCount() =
    1

  override fun getColumnName(column: Int) =
    ""

  override fun getRowCount() =
    internalProviders.size

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
    if (rowIndex >= 0 && rowIndex < internalProviders.size) {
      internalProviders[rowIndex]
    } else {
      null
    }

  override fun canExchangeRows(oldIndex: Int, newIndex: Int) =
    true

  override fun exchangeRows(oldIndex: Int, newIndex: Int) {
    internalProviders.add(newIndex, internalProviders.removeAt(oldIndex))
    fireTableRowsUpdated(oldIndex.coerceAtMost(newIndex), oldIndex.coerceAtLeast(newIndex))
  }
}
