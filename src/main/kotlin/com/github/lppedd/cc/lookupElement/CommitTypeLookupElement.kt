package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.api.CommitType
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
    private val psiElement: CommitTypePsiElement,
    private val commitType: CommitType,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    commitType

  override fun getPsiElement(): CommitTypePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitType.getValue()

  override fun getItemText(): String =
    commitType.getText()

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = CCIcons.Tokens.Type
    super.renderElement(presentation)
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
          commitType.getValue(),
      )
    } else {
      editor.insertStringAtCaret(commitType.getValue())
    }
  }
}
