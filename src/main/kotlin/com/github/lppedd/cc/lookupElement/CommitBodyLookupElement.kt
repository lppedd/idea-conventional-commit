package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitBody
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import kotlin.math.max

/**
 * @author Edoardo Luppi
 */
internal class CommitBodyLookupElement(
  private val psiElement: CommitBodyPsiElement,
  private val commitBody: CommitBody,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    commitBody

  override fun getPsiElement(): CommitBodyPsiElement =
    psiElement

  override fun getLookupString(): String =
    commitBody.getValue()

  override fun getItemText(): String =
    commitBody.getText()

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = CC.Icon.Token.Body
    super.renderElement(presentation)
    presentation.itemText = commitBody.getText().flattenWhitespaces().abbreviate(100)
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = editor.document
    val caretOffset = editor.caretModel.offset
    val lineStartOffset = document.getLineRangeByOffset(caretOffset).startOffset
    val lineCount = document.lineCount
    val currentLineNumber = document.getLineNumber(caretOffset)

    var replaceUntilOffset = -1
    var addNewLine = false

    // TODO: simplify. This is like a drug trip currently
    // We scan the document line by line until we find a footer (this means there were
    // no bodies and we are going to overwrite that footer),
    // or a blank line (that means we simply overwrite everything up to that point)
    for (i in currentLineNumber until lineCount) {
      val (currLineStartOffset, currLineEndOffset, currLineIsEmpty) = document.getLineRange(i)

      if (currLineIsEmpty) {
        // -1 to return to the previous line end
        replaceUntilOffset = max(currLineStartOffset - 1, lineStartOffset)
        addNewLine = i == currentLineNumber && i + 1 < lineCount && !document.isLineEmpty(i + 1)
        break
      }

      // Check if this line represents a footer
      val (_, separator) = CCParser.parseFooter(document.getSegment(currLineStartOffset, currLineEndOffset))

      if (separator.isPresent) {
        if (replaceUntilOffset != -1) {
          replaceUntilOffset = currLineStartOffset - 1
          break
        }

        replaceUntilOffset = currLineEndOffset
        addNewLine = true
      }

      // If we are at the last line, just overwrite until its end offset
      if (i == lineCount - 1) {
        replaceUntilOffset = currLineEndOffset
        addNewLine = false
      }
    }

    if (replaceUntilOffset >= lineStartOffset) {
      editor.replaceString(lineStartOffset, replaceUntilOffset, commitBody.getValue())

      if (addNewLine) {
        editor.insertStringAtCaret("\n", moveCaret = false)
      }
    }
  }
}
