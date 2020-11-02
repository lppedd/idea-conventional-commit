package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.FooterValueProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.util.TextRange

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterValueLookupElement(
    index: Int,
    provider: FooterValueProviderWrapper,
    private val psiElement: CommitFooterValuePsiElement,
    private val completionPrefix: String,
) : CommitLookupElement(index, PRIORITY_FOOTER_VALUE, provider) {
  private val commitFooterValue = psiElement.commitFooterValue

  override fun getPsiElement(): CommitFooterValuePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitFooterValue.text

  override fun renderElement(presentation: LookupElementPresentation) =
    presentation.let {
      it.icon = CCIcons.Tokens.Footer
      it.itemText = lookupString.flattenWhitespaces().abbreviate(100)
      it.isTypeIconRightAligned = true

      val rendering = commitFooterValue.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }

  override fun handleInsert(context: InsertionContext) {
    val document = context.document
    val (lineStart, lineEnd) = document.getLineRangeByOffset(context.startOffset)
    val elementValue = commitFooterValue.getValue(context.toTokenContext())
    val tempAdditionalLength = elementValue.length - completionPrefix.length
    val removeTo = context.tailOffset - lineStart
    val removeFrom = removeTo - tempAdditionalLength
    val oldFooterText =
      document
        .getSegment(lineStart, document.textLength)
        .removeRange(removeFrom, removeTo)

    val footer = CCParser.parseFooter(oldFooterText).footer
    val footerText = " $elementValue"
    val (footerStart, footerEnd) = if (footer is ValidToken) {
      val (start, end) = footer.range
      TextRange(lineStart + start, lineStart + end + tempAdditionalLength)
    } else {
      TextRange(lineStart, lineEnd)
    }

    document.replaceString(footerStart, footerEnd, footerText)
    context.editor.moveCaretToOffset(footerStart + footerText.length)
  }
}
