package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCUI
import com.github.lppedd.cc.api.CommitTokenProvider
import com.intellij.ui.table.JBTable
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent

/**
 * @author Edoardo Luppi
 */
internal class CommitProviderTable<T : CommitTokenProvider> : JBTable() {
  companion object {
    const val COLUMN_PROVIDER = 0
  }

  private val model = CommitProviderModel<T>()
  private var latest = emptyList<T>()

  init {
    setModel(model)
    emptyText.text = CCBundle["cc.config.providers.empty"]

    background = CCUI.ListBackgroundColor
    setShowGrid(false)
    setTableHeader(null)
    getColumnModel().getColumn(COLUMN_PROVIDER).also {
      it.minWidth = 100
      it.cellRenderer = CommitProviderRenderer()
    }

    addFocusListener(object : FocusAdapter() {
      override fun focusLost(event: FocusEvent?) {
        clearSelection()
      }
    })
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
