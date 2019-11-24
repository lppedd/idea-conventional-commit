package com.github.lppedd.cc.configuration.component.tokens

import javax.swing.AbstractListModel

/**
 * @author Edoardo Luppi
 */
internal class CommitTokenModel : AbstractListModel<String>() {
  companion object {
    private const val serialVersionUID = 1L
  }

  private var tokens: List<String> = emptyList()

  fun setTokens(tokens: List<String>) {
    val oldSize = this.tokens.size
    this.tokens = tokens
    fireContentsChanged(tokens, 0, oldSize - 1)
  }

  override fun getSize() = tokens.size
  override fun getElementAt(index: Int) = tokens[index]
}
