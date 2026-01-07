package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitFooterValue
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterValueLookupElement(
  private val psiElement: CommitFooterValuePsiElement,
  private val commitFooterValue: CommitFooterValue,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    commitFooterValue

  override fun getPsiElement(): CommitFooterValuePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitFooterValue.getValue()

  override fun getItemText(): String =
    commitFooterValue.getText()

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = CC.Icon.Token.Footer
    super.renderElement(presentation)
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = editor.document
    val (lineStartOffset) = editor.getCurrentLineRange()
    val lineText = document.getSegment(lineStartOffset, document.textLength)
    val footer = CCParser.parseFooter(lineText).footer
    val newFooterValueString = " ${commitFooterValue.getValue()}"

    if (footer is ValidToken) {
      // Replace an existing footer value
      editor.replaceString(
        lineStartOffset + footer.range.startOffset,
        lineStartOffset + footer.range.endOffset,
        newFooterValueString,
      )
    } else {
      // No footer value was present, just insert the string
      editor.insertStringAtCaret(newFooterValueString)
    }
  }
}
