package com.github.lppedd.cc.configuration.holders

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.ICON_ARROW_RIGHT
import com.github.lppedd.cc.ICON_SCOPE
import com.github.lppedd.cc.ICON_TYPE
import com.github.lppedd.cc.configuration.CCDefaultTokensService.JsonCommitScope
import com.github.lppedd.cc.configuration.CommitTypeMap
import com.github.lppedd.cc.configuration.component.tokens.CommitTokenList
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Graphics
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JList
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class DefaultsListsHolder : ComponentHolder {
  private lateinit var latestTokens: CommitTypeMap
  private val panel = JPanel(BorderLayout())
  private val scopeList = CommitTokenList(ICON_SCOPE)
  private val typeList = CommitTokenList(ICON_TYPE).apply {
    addListSelectionListener { onCommitTypeChanged() }
  }

  override fun getComponent() = buildComponents()

  fun setTokens(tokens: CommitTypeMap) {
    latestTokens = tokens
    typeList.setTokens(tokens.keys)
    onCommitTypeChanged()
  }

  private fun buildComponents(): JPanel {
    ListSpeedSearch(typeList)
    ListSpeedSearch(scopeList)

    val innerTokensPanel = object : JPanel(GridLayout(1, 1, /* Lists h-gap */ 30, 1)) {
      override fun paint(g: Graphics) {
        super.paint(g)
        val x = (width - ICON_ARROW_RIGHT.iconWidth) / 2
        val y = (height - ICON_ARROW_RIGHT.iconHeight + 20) / 2
        ICON_ARROW_RIGHT.paintIcon(this, g, x, y)
      }
    }

    innerTokensPanel.add(createTokensPanel(typeList, CCBundle["cc.config.types"]))
    innerTokensPanel.add(createTokensPanel(scopeList, CCBundle["cc.config.scopes"]))

    return panel.apply {
      add(innerTokensPanel)
    }
  }

  private fun onCommitTypeChanged() {
    val selectedValue: String? = typeList.selectedValue

    if (selectedValue != null) {
      val jsonCommitType = latestTokens[selectedValue] ?: return
      val scopes = jsonCommitType.scopes.map(JsonCommitScope::name)
      scopeList.setTokens(scopes)
    }
  }

  private fun createTokensPanel(list: JList<String>, title: String): JPanel {
    val border = BorderFactory.createCompoundBorder(
      IdeBorderFactory.createBorder(SideBorder.ALL),
      BorderFactory.createEmptyBorder(0, 1, 1, 1)
    )

    val scrollablePanel = JBScrollPane(list)
    scrollablePanel.border = border

    val panel = JPanel(BorderLayout())
    panel.border = IdeBorderFactory.createTitledBorder(title, false, JBUI.insetsTop(7))
    panel.minimumSize = JBDimension(130, 150)
    panel.add(scrollablePanel)

    return panel
  }
}
