package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.TypeProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * Represents a commit type item in the completion's popup.
 *
 * @author Edoardo Luppi
 */
internal open class CommitTypeLookupElement(
    index: Int,
    provider: TypeProviderWrapper,
    private val psiElement: CommitTypePsiElement,
) : CommitLookupElement(index, CC.Tokens.PriorityType, provider) {
  private val commitType = psiElement.commitType

  override fun getPsiElement(): CommitTypePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitType.value

  override fun getDisplayedText(): String =
    commitType.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.icon = CCIcons.Tokens.Type
      it.itemText = getDisplayedText()
      it.isTypeIconRightAligned = true

      val rendering = commitType.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val (lineStartOffset, lineEndOffset) = editor.getCurrentLineRange()
    val lineText = editor.document.getSegment(lineStartOffset, lineEndOffset)
    val fixedLineText = CCParser.fixLine(lineText, editor.getCaretOffsetInLine()).trimEnd()
    val type = CCParser.parseHeader(fixedLineText).type

    if (type is ValidToken) {
      // Replace the old type with the new one
      editor.replaceString(
        lineStartOffset + type.range.startOffset,
        lineStartOffset + type.range.endOffset,
        commitType.value,
      )
    } else {
      editor.insertStringAtCaret(commitType.value)
    }
  }
}
