package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.FooterTypeProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterTypeLookupElement(
    index: Int,
    provider: FooterTypeProviderWrapper,
    private val psiElement: CommitFooterTypePsiElement,
) : CommitLookupElement(index, PRIORITY_FOOTER_TYPE, provider) {
  private val commitFooterType = psiElement.commitFooterType

  override fun getPsiElement(): CommitFooterTypePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitFooterType.text

  override fun renderElement(presentation: LookupElementPresentation) =
    presentation.let {
      it.icon = CCIcons.Tokens.Footer
      it.itemText = lookupString
      it.isItemTextBold = true
      it.isTypeIconRightAligned = true

      val rendering = commitFooterType.getRendering()
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document

    val range = editor.getCurrentLineRange()
    val lineText = document.getText(range)
    val (footerType, separator, footerValue) = CCParser.parseFooter(lineText)
    val caretShift: Int
    val textToAdd: String

    if (footerType is ValidToken) {
      if (separator.isPresent) {
        if (footerValue !is ValidToken) {
          caretShift = 2
          textToAdd = " "
        } else {
          caretShift = if (footerValue.value.firstIsWhitespace()) 2 else 1
          textToAdd = ""
        }
      } else {
        caretShift = 2
        textToAdd = ": "
      }

      val elementValue = commitFooterType.getValue(context.toTokenContext())
      val text = footerType.range.replace(lineText, elementValue)
      document.replaceString(context.startOffset, range.endOffset, "$text$textToAdd")
    } else {
      document.insertString(context.tailOffset, ": ")
      caretShift = 2
    }

    editor.moveCaretRelatively(caretShift)
    editor.scheduleAutoPopup()
  }
}
