package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCEditorUtils
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.CCParser
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.runWriteAction

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectLookupElement(
  override val index: Int,
  private val psi: CommitSubjectPsiElement
) : CommitLookupElement() {
  override val weight = 10

  override fun getPsiElement() = psi
  override fun getLookupString() = psi.commitSubject.text
  override fun renderElement(presentation: LookupElementPresentation) {
    val commitSubject = psi.commitSubject
    val rendering = commitSubject.getRendering()
    presentation.run {
      itemText = commitSubject.text
      icon = CCIcons.SUBJECT
      isItemTextBold = rendering.bold
      isItemTextItalic = rendering.italic
      isStrikeout = rendering.strikeout
      isTypeIconRightAligned = true
      setTypeText(rendering.type, rendering.icon)
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document
    val lineRange = CCEditorUtils.getCurrentLineRange(editor)
    val lineText = document.text.substring(lineRange.first, lineRange.last)

    val subject = CCParser.parseText(lineText).subject
    val subjectStartOffset = lineRange.first + subject.range.first
    val newSubjectStr = " $lookupString"

    runWriteAction {
      document.replaceString(subjectStartOffset, lineRange.last, newSubjectStr)
    }

    editor.caretModel.moveToOffset(subjectStartOffset + newSubjectStr.length)
  }
}
