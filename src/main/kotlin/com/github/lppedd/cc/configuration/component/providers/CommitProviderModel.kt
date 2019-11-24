package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.configuration.component.AbstractTableEditableModel

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderModel<T : CommitTokenProvider>(
  private val title: String
) : AbstractTableEditableModel() {
  companion object {
    private const val serialVersionUID = 1L
  }

  private var providers = mutableListOf<T>()

  fun getProviders() = providers
  fun setProviders(providers: MutableList<T>) {
    this.providers = providers
    fireTableDataChanged()
  }

  override fun getColumnCount() = 1
  override fun getColumnName(column: Int) =
    if (column == CommitProviderTable.COLUMN_PROVIDER) title
    else ""

  override fun getRowCount() = providers.size
  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
    if (rowIndex >= 0 && rowIndex < providers.size) {
      providers[rowIndex]
    } else {
      null
    }

  override fun canExchangeRows(oldIndex: Int, newIndex: Int) = true
  override fun exchangeRows(oldIndex: Int, newIndex: Int) {
    providers.add(newIndex, providers.removeAt(oldIndex))
    fireTableRowsUpdated(oldIndex.coerceAtMost(newIndex), oldIndex.coerceAtLeast(newIndex))
  }
}
