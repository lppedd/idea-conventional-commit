package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.FooterValueProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterValueLookupElement(
    index: Int,
    provider: FooterValueProviderWrapper,
    private val psiElement: CommitFooterValuePsiElement,
) : CommitLookupElement(index, CC.Tokens.PriorityFooterValue, provider) {
  private val commitFooterValue = psiElement.commitFooterValue

  override fun getPsiElement(): CommitFooterValuePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitFooterValue.value

  override fun getDisplayedText(): String =
    commitFooterValue.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.icon = CCIcons.Tokens.Footer
      it.itemText = getDisplayedText().flattenWhitespaces().abbreviate(100)
      it.isTypeIconRightAligned = true

      val rendering = commitFooterValue.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = editor.document
    val (lineStartOffset) = editor.getCurrentLineRange()
    val lineText = document.getSegment(lineStartOffset, document.textLength)
    val footer = CCParser.parseFooter(lineText).footer
    val newFooterValueString = " ${commitFooterValue.value}"

    if (footer is ValidToken) {
      // Replace an existing footer value
      editor.replaceString(
          lineStartOffset + footer.range.startOffset,
          lineStartOffset + footer.range.endOffset,
          newFooterValueString,
      )
    } else {
      // No footer value was present, just insert the string
      editor.insertStringAtCaret(newFooterValueString)
    }
  }
}
