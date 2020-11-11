package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.ui.CCTable

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderTable<T : CommitTokenProvider> : CCTable() {
  companion object {
    const val COLUMN_PROVIDER = 0
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
}
