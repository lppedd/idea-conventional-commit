package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.SubjectProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectLookupElement(
    index: Int,
    provider: SubjectProviderWrapper,
    private val psiElement: CommitSubjectPsiElement,
) : CommitLookupElement(index, CC.Tokens.PrioritySubject, provider) {
  private val commitSubject = psiElement.commitSubject

  override fun getPsiElement(): CommitSubjectPsiElement =
    psiElement

  override fun getLookupString(): String =
    commitSubject.value

  override fun getDisplayedText(): String =
    commitSubject.text

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.icon = CCIcons.Tokens.Subject
      it.itemText = getDisplayedText()
      it.isTypeIconRightAligned = true

      val rendering = commitSubject.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val (lineStartOffset, lineEndOffset) = editor.getCurrentLineRange()
    val line = context.document.getSegment(lineStartOffset, lineEndOffset)
    val subject = CCParser.parseHeader(line).subject
    val newSubjectString = " ${commitSubject.value}"

    if (subject is ValidToken) {
      // Replace an existing subject
      editor.replaceString(
        lineStartOffset + subject.range.startOffset,
        lineEndOffset,
        newSubjectString,
      )
    } else {
      // No subject was present before, just insert the string
      editor.insertStringAtCaret(newSubjectString)
    }
  }
}
