package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitFooterType
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * Represents an item in the completion popup inside the footer type context.
 *
 * @author Edoardo Luppi
 */
internal class CommitFooterTypeLookupElement(
    private val psiElement: CommitFooterTypePsiElement,
    private val commitFooterType: CommitFooterType,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    commitFooterType

  override fun getPsiElement(): CommitFooterTypePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitFooterType.getValue()

  override fun getItemText(): String =
    commitFooterType.getText()

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = CCIcons.Tokens.Footer
    super.renderElement(presentation)
  }

  @Suppress("DuplicatedCode")
  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val (lineStartOffset, lineEndOffset) = editor.getCurrentLineRange()
    val lineText = editor.document.getSegment(lineStartOffset, lineEndOffset)
    val (footerType, separator, footerValue) = CCParser.parseFooter(lineText)

    if (footerType is ValidToken) {
      // Replace the old footer type with the new one
      editor.replaceString(
          lineStartOffset + footerType.range.startOffset,
          lineStartOffset + footerType.range.endOffset,
          commitFooterType.getValue(),
      )
    } else {
      // No footer type had been inserted before, thus we simply insert the value
      editor.insertStringAtCaret(commitFooterType.getValue())
    }

    // If a separator isn't already present, add it
    if (!separator.isPresent) {
      editor.insertStringAtCaret(":", moveCaret = false)
    }

    // Move the caret after the separator
    editor.moveCaretRelatively(1)

    // If the footer value is present and starts with whitespace,
    // shift the caret of one position, otherwise insert whitespace
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
