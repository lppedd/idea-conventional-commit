package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.BodyProviderWrapper
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import kotlin.math.max

/**
 * @author Edoardo Luppi
 */
internal class CommitBodyLookupElement(
    index: Int,
    provider: BodyProviderWrapper,
    private val psiElement: CommitBodyPsiElement,
) : CommitLookupElement(index, CC.Tokens.PriorityBody, provider) {
  private val commitBody = psiElement.commitBody

  override fun getPsiElement(): CommitBodyPsiElement =
    psiElement

  override fun getLookupString(): String =
    commitBody.value

  override fun getDisplayedText(): String =
    commitBody.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.icon = CCIcons.Tokens.Body
      it.itemText = lookupString.flattenWhitespaces().abbreviate(100)
      it.isTypeIconRightAligned = true

      val rendering = commitBody.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = editor.document
    val caretOffset = editor.caretModel.offset
    val lineStartOffset = document.getLineRangeByOffset(caretOffset).startOffset

    for (i in document.getLineNumber(caretOffset) until document.lineCount) {
      val (_, lineEndOffset, lineIsEmpty) = document.getLineRange(i)

      if (lineIsEmpty) {
        editor.replaceString(lineStartOffset, max(lineStartOffset, lineEndOffset), commitBody.value)
        //editor.insertStringAtCaret(commitBody.value)
        return
      } else {
      }

    }
  }
}
