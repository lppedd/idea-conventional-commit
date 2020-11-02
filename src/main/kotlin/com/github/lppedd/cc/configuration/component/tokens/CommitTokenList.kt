package com.github.lppedd.cc.configuration.component.tokens

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.LIST_BACKGROUND_COLOR
import com.intellij.ui.components.JBList
import javax.swing.Icon
import javax.swing.ListSelectionModel

/**
 * @author Edoardo Luppi
 */
internal class CommitTokenList(icon: Icon) : JBList<String>() {
  private val model = CommitTokenModel()

  init {
    setModel(model)
    background = LIST_BACKGROUND_COLOR
    cellRenderer = CommitTokenCellRenderer(icon)
    selectionMode = ListSelectionModel.SINGLE_SELECTION
    emptyText.text = CCBundle["cc.config.defaults.empty"]
  }

  fun setTokens(tokens: Collection<String>) {
    model.setTokens(tokens.toList())
    selectedIndex = 0
  }
}
