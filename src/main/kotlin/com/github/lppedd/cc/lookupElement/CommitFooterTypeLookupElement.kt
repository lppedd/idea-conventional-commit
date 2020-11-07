package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.FooterTypeProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * Represents an item in the completion's popup inside the footer type context.
 *
 * @author Edoardo Luppi
 */
internal class CommitFooterTypeLookupElement(
    index: Int,
    provider: FooterTypeProviderWrapper,
    private val psiElement: CommitFooterTypePsiElement,
) : CommitLookupElement(index, CC.Tokens.PriorityFooterType, provider) {
  private val commitFooterType = psiElement.commitFooterType

  override fun getPsiElement(): CommitFooterTypePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitFooterType.value

  override fun getDisplayedText(): String =
    commitFooterType.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.icon = CCIcons.Tokens.Footer
      it.itemText = getDisplayedText()
      it.isItemTextBold = true
      it.isTypeIconRightAligned = true

      val rendering = commitFooterType.getRendering()
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }
  }

  @Suppress("DuplicatedCode")
  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val (lineStartOffset, lineEndOffset) = editor.getCurrentLineRange()
    val lineText = editor.document.getSegment(lineStartOffset, lineEndOffset)
    val (footerType, separator, footerValue) = CCParser.parseFooter(lineText)

    if (footerType is ValidToken) {
      // Replace the old footer type with new one
      editor.replaceString(
        lineStartOffset + footerType.range.startOffset,
        lineStartOffset + footerType.range.endOffset,
        commitFooterType.value,
      )
    } else {
      // No footer type had been inserted before, thus we simply insert the value
      editor.insertStringAtCaret(commitFooterType.value)
    }

    // If a separator isn't already present, add it
    if (!separator.isPresent) {
      editor.insertStringAtCaret(":", moveCaret = false)
    }

    // Move the caret after the separator
    editor.moveCaretRelatively(1)

    // If the footer value is present and starts with a whitespace,
    // shift the caret of one position, otherwise insert a whitespace
    if (footerValue is ValidToken) {
      if (footerValue.value.firstIsWhitespace()) {
        editor.moveCaretRelatively(1)
      }
    } else {
      editor.insertStringAtCaret(" ")
    }

    editor.scheduleAutoPopup()
  }
}
