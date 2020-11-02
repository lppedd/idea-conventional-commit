package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.TypeProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.parser.isInContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
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
    commitType.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.icon = CCIcons.Tokens.Type
      it.itemText = lookupString
      it.isTypeIconRightAligned = true

      val rendering = commitType.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val document = context.document
    val documentText = document.immutableCharSequence
    val elementValue = commitType.getValue(context.toTokenContext())
    val (lineStart, lineEnd) = context.editor.getCurrentLineRange()
    val startingCaretOffset = context.tailOffset - lineStart - elementValue.length
    val lineText = documentText.subSequence(lineStart, lineEnd)
    val type = CCParser.parseHeader(lineText).type

    if (type is ValidToken && type.isInContext(startingCaretOffset)) {
      if (type.value != elementValue) {
        val text = type.range.replace("$lineText", elementValue)
        document.replaceString(lineStart, lineEnd, text)
      }
    } else {
      val startOffset = context.startOffset
      document.replaceString(
        startOffset,
        maxOf(documentText.indexOf(' ', startOffset), 0),
        elementValue
      )
    }
  }
}
