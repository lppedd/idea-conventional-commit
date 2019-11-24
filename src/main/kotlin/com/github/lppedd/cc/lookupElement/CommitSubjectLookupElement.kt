package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectLookupElement(
  override val index: Int,
  private val psi: CommitSubjectPsiElement
) : CommitLookupElement() {
  override val weight = 10

  override fun getPsiElement() = psi
  override fun getLookupString() = psi.commitSubject.text
  override fun renderElement(presentation: LookupElementPresentation) {
    val commitSubject = psi.commitSubject
    val rendering = commitSubject.getRendering()
    presentation.run {
      itemText = commitSubject.text
      icon = CCIcons.SUBJECT
      isItemTextBold = rendering.bold
      isItemTextItalic = rendering.italic
      isStrikeout = rendering.strikeout
      isTypeIconRightAligned = true
      setTypeText(rendering.type, rendering.icon)
    }
  }
}
