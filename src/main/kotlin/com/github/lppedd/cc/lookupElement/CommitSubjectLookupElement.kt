package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitSubject
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectLookupElement(
    private val psiElement: CommitSubjectPsiElement,
    private val commitSubject: CommitSubject,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    commitSubject

  override fun getPsiElement(): CommitSubjectPsiElement =
    psiElement

  override fun getLookupString(): String =
    commitSubject.getValue()

  override fun getItemText(): String =
    commitSubject.getText()

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = CC.Icon.Token.Subject
    super.renderElement(presentation)
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val (lineStartOffset, lineEndOffset) = editor.getCurrentLineRange()
    val line = context.document.getSegment(lineStartOffset, lineEndOffset)
    val subject = CCParser.parseHeader(line).subject
    val newSubjectString = " ${commitSubject.getValue()}"

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
