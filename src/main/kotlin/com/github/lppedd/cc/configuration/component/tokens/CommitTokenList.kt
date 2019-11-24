package com.github.lppedd.cc.configuration.component.tokens

import com.github.lppedd.cc.CCBundle
import com.intellij.ui.components.JBList
import java.awt.Dimension
import javax.swing.Icon
import javax.swing.ListSelectionModel

/**
 * @author Edoardo Luppi
 */
internal class CommitTokenList(icon: Icon) : JBList<String>() {
  private val model = CommitTokenModel()

  init {
    setModel(model)
    minimumSize = Dimension(130, 150)
    cellRenderer = CommitTokenCellRenderer(icon)
    selectionMode = ListSelectionModel.SINGLE_SELECTION
    emptyText.text = CCBundle["cc.config.defaults.empty"]
  }

  fun setTokens(tokens: Collection<String>) {
    model.setTokens(tokens.toList())
    selectedIndex = 0
  }
}
