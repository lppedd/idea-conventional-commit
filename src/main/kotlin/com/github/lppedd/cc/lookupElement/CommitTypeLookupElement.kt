package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal open class CommitTypeLookupElement(
  override val index: Int,
  private val psi: CommitTypePsiElement
) : CommitLookupElement() {
  override val weight = 30

  override fun getPsiElement() = psi
  override fun getLookupString() = psi.commitType.text
  override fun renderElement(presentation: LookupElementPresentation) {
    val commitType = psi.commitType
    val rendering = commitType.getRendering()
    presentation.run {
      itemText = commitType.text
      icon = CCIcons.TYPE
      isItemTextBold = rendering.bold
      isItemTextItalic = rendering.italic
      isStrikeout = rendering.strikeout
      isTypeIconRightAligned = true
      setTypeText(rendering.type, rendering.icon)
    }
  }
}
