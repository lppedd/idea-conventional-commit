package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.CCDefaultTokensService.JsonCommitScope
import com.github.lppedd.cc.configuration.CommitTypeMap
import com.github.lppedd.cc.configuration.component.tokens.CommitTokenList
import com.github.lppedd.cc.ui.JBGridLayout
import com.github.lppedd.cc.ui.TitledPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.Graphics
import javax.swing.JList
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class DefaultTokensPanel : JPanel(JBGridLayout(1, 1, 24, 1)) {
  private var latestTokens: CommitTypeMap = emptyMap()
  private val scopeList = CommitTokenList(CCIcons.Tokens.Scope)
  private val typeList = CommitTokenList(CCIcons.Tokens.Type).also {
    it.addListSelectionListener {
      onCommitTypeChanged()
    }
  }

  init {
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
    ListSpeedSearch(list)

    val scrollPane = JBScrollPane(list).also {
      it.preferredSize = list.minimumSize
      it.border = JBUI.Borders.customLine(CCUI.BorderColor).wrap(JBUI.Borders.empty(0, 1, 1, 1))
    }

    return TitledPanel(title, scrollPane).also {
      it.minimumSize = JBDimension(130, 250)
    }
  }

  override fun paint(g: Graphics) {
    super.paint(g)

    val icon = CCIcons.General.ArrowRight
    val x = (width - icon.iconWidth) / 2
    // 32 is a magic constant found by trial and error
    val y = (height - icon.iconHeight + 32.scaled) / 2
    icon.paintIcon(this, g, x, y)
  }
}
