package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterLookupElement(
    override val index: Int,
    private val psiElement: CommitFooterPsiElement,
    private val completionPrefix: String,
) : CommitLookupElement() {
  override val weight: UInt = WEIGHT_FOOTER

  override fun getPsiElement(): PsiElement =
    psiElement

  override fun getLookupString(): String =
    psiElement.commitFooter.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = ICON_FOOTER
    presentation.itemText = lookupString.flattenWhitespaces().abbreviate(100)
    presentation.isTypeIconRightAligned = true
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document

    val (lineStart, lineEnd) = document.getLineRangeByOffset(context.startOffset)
    val tempAdditionalLength = lookupString.length - completionPrefix.length
    val removeTo = context.tailOffset - lineStart
    val removeFrom = removeTo - tempAdditionalLength
    val oldFooterText =
      document
        .getSegment(lineStart until document.textLength)
        .removeRange(removeFrom, removeTo)

    val footer = CCParser.parseFooter(oldFooterText).footer
    val footerText = " $lookupString"
    val footerRange = if (footer is ValidToken) {
      val (start, end) = footer.range
      lineStart + start until lineStart + end + tempAdditionalLength
    } else {
      lineStart until lineEnd
    }

    document.replaceString(footerRange.first, footerRange.last + 1, footerText)
    editor.moveCaretToOffset(footerRange.first + footerText.length)
  }
}
