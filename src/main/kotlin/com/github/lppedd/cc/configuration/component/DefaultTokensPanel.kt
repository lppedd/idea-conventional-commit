package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.CCDefaultTokensService.JsonCommitScope
import com.github.lppedd.cc.configuration.CommitTypeMap
import com.github.lppedd.cc.configuration.component.tokens.CommitTokenList
import com.github.lppedd.cc.ui.JBGridLayout
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Graphics
import javax.swing.JList
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class DefaultTokensPanel : JPanel(JBGridLayout(1, 1, 24, 1)) {
  private var latestTokens: CommitTypeMap = emptyMap()
  private val scopeList = CommitTokenList(ICON_SCOPE)
  private val typeList = CommitTokenList(ICON_TYPE).also {
    it.addListSelectionListener {
      onCommitTypeChanged()
    }
  }

  init {
    ListSpeedSearch(typeList)
    ListSpeedSearch(scopeList)

    add(createTokenListPanel(typeList, CCBundle["cc.config.types"]))
    add(createTokenListPanel(scopeList, CCBundle["cc.config.scopes"]))
  }

  fun setTokens(tokens: CommitTypeMap) {
    latestTokens = tokens
    typeList.setTokens(tokens.keys)
    onCommitTypeChanged()
  }

  private fun onCommitTypeChanged() {
    val selectedValue: String? = typeList.selectedValue

    if (selectedValue != null) {
      val jsonCommitType = latestTokens[selectedValue] ?: return
      val scopes = jsonCommitType.scopes.map(JsonCommitScope::name)
      scopeList.setTokens(scopes)
    }
  }

  private fun createTokenListPanel(list: JList<String>, title: String): JPanel {
    val scrollPane = JBScrollPane(list).also {
      it.preferredSize = list.minimumSize
      it.border = IdeBorderFactory.createBorder(BORDER_COLOR).wrap(JBUI.Borders.empty(0, 1, 1, 1))
    }

    return JPanel(BorderLayout()).also {
      val border = IdeBorderFactory.createTitledBorder(title, false, JBUI.insetsTop(7))
      border.setShowLine(false)

      it.border = border
      it.minimumSize = JBDimension(130, 250)
      it.add(scrollPane)
    }
  }

  override fun paint(g: Graphics) {
    super.paint(g)
    val x = (width - ICON_ARROW_RIGHT.iconWidth) / 2
    val y = (height - ICON_ARROW_RIGHT.iconHeight + 20) / 2
    ICON_ARROW_RIGHT.paintIcon(this, g, x, y)
  }
}
