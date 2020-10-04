package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.BodyProviderWrapper
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitBodyLookupElement(
    index: Int,
    provider: BodyProviderWrapper,
    private val psiElement: CommitBodyPsiElement,
    private val completionPrefix: String,
) : CommitLookupElement(index, PRIORITY_BODY, provider) {
  private val commitBody = psiElement.commitBody

  override fun getPsiElement(): CommitBodyPsiElement =
    psiElement

  override fun getLookupString(): String =
    commitBody.text

  override fun renderElement(presentation: LookupElementPresentation) =
    presentation.let {
      it.icon = ICON_BODY
      it.itemText = lookupString.flattenWhitespaces().abbreviate(100)
      it.isTypeIconRightAligned = true

      val rendering = commitBody.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }

  override fun handleInsert(context: InsertionContext) {
    val document = context.document
    val caretOffset = context.startOffset
    val lineStart = document.getLineRangeByOffset(caretOffset).startOffset
    val elementValue = commitBody.getValue(context.toTokenContext())

    if (completionPrefix.isNotEmpty()) {
      val tempAdditionalLength = elementValue.length - completionPrefix.length
      val removeTo = context.tailOffset
      val removeFrom = context.tailOffset - tempAdditionalLength
      document.deleteString(removeFrom, removeTo)
    }

    for (i in document.getLineNumber(caretOffset) until document.lineCount) {
      val (_, end, isEmpty) = document.getLineRange(i)

      if (isEmpty) {
        document.replaceString(lineStart, end - 1, elementValue)
        context.editor.moveCaretToOffset(lineStart + elementValue.length)
        return
      }
    }
  }
}
