package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.runWriteAction

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectLookupElement(
    override val index: Int,
    private val psi: CommitSubjectPsiElement,
) : CommitLookupElement() {
  override val weight = 10
  override fun getPsiElement() = psi
  override fun getLookupString() = psi.commitSubject.text

  override fun renderElement(presentation: LookupElementPresentation) {
    val commitSubject = psi.commitSubject
    val rendering = commitSubject.getRendering()
    presentation.itemText = commitSubject.text
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
    val lineText = document.getText(lineStart to lineEnd)
    val subject = CCParser.parseText(lineText).subject
    val subjectStartOffset = lineStart + ((subject as? ValidToken)?.range?.first ?: 0)
    val subjectText = " $lookupString"

    runWriteAction {
      document.replaceString(subjectStartOffset, lineEnd, subjectText)
    }

    editor.moveCaretToOffset(subjectStartOffset + subjectText.length)
  }
}
