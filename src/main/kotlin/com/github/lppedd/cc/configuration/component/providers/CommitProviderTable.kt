package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitTokenProvider
import com.intellij.ui.table.JBTable

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderTable<T : CommitTokenProvider>(title: String) : JBTable() {
  companion object {
    const val COLUMN_PROVIDER = 0
  }

  private val model = CommitProviderModel<T>(title)
  private var latest = emptyList<T>()

  init {
    setModel(model)

    isStriped = true
    emptyText.text = CCBundle["cc.config.providers.empty"]

    getTableHeader().apply {
      resizingAllowed = false
      reorderingAllowed = false
    }

    getColumnModel().getColumn(COLUMN_PROVIDER).apply {
      minWidth = 100
      cellRenderer = CommitProviderRenderer<T>()
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
}
