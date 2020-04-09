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
    override val index: Int,
    override val provider: BodyProviderWrapper,
    private val psiElement: CommitBodyPsiElement,
    private val completionPrefix: String,
) : CommitLookupElement() {
  override val priority = PRIORITY_BODY

  override fun getPsiElement(): CommitBodyPsiElement =
    psiElement

  override fun getLookupString(): String =
    psiElement.commitBody.value

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = ICON_BODY
    presentation.itemText = lookupString.flattenWhitespaces().abbreviate(100)
    presentation.isTypeIconRightAligned = true
  }

  override fun handleInsert(context: InsertionContext) {
    val document = context.document
    val caretOffset = context.startOffset
    val lineStart = document.getLineRangeByOffset(caretOffset).startOffset

    if (completionPrefix.isNotEmpty()) {
      val tempAdditionalLength = lookupString.length - completionPrefix.length
      val removeTo = context.tailOffset
      val removeFrom = context.tailOffset - tempAdditionalLength
      document.deleteString(removeFrom, removeTo)
    }

    for (i in document.getLineNumber(caretOffset) until document.lineCount) {
      val (_, end, isEmpty) = document.getLineRange(i)

      if (isEmpty) {
        document.replaceString(lineStart, end - 1, lookupString)
        context.editor.moveCaretToOffset(lineStart + lookupString.length)
        return
      }
    }
  }
}
