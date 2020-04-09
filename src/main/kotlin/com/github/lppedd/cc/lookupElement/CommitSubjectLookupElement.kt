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
    override val index: Int,
    override val provider: SubjectProviderWrapper,
    private val psiElement: CommitSubjectPsiElement,
) : CommitLookupElement() {
  override val priority = PRIORITY_SUBJECT

  override fun getPsiElement(): CommitSubjectPsiElement =
    psiElement

  override fun getLookupString(): String =
    psiElement.commitSubject.value

  override fun renderElement(presentation: LookupElementPresentation) {
    val commitSubject = psiElement.commitSubject
    val rendering = commitSubject.getRendering()
    presentation.itemText = commitSubject.value
    presentation.icon = ICON_SUBJECT
    presentation.isItemTextBold = rendering.bold
    presentation.isItemTextItalic = rendering.italic
    presentation.isStrikeout = rendering.strikeout
    presentation.isTypeIconRightAligned = true
    presentation.setTypeText(rendering.type, rendering.icon)
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document

    val (lineStart, lineEnd) = editor.getCurrentLineRange()
    val line = document.getSegment(lineStart, lineEnd)
    val subject = CCParser.parseHeader(line).subject
    val subjectStartOffset = lineStart + ((subject as? ValidToken)?.range?.startOffset ?: 0)
    val subjectText = " $lookupString"

    document.replaceString(subjectStartOffset, lineEnd, subjectText)
    editor.moveCaretToOffset(subjectStartOffset + subjectText.length)
  }
}
