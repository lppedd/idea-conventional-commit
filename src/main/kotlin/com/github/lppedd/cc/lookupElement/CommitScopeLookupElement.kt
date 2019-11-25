package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCEditorUtils
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.CCParser
import com.github.lppedd.cc.psi.CommitScopePsiElement
import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.runWriteAction

/**
 * @author Edoardo Luppi
 */
internal open class CommitScopeLookupElement(
  override val index: Int,
  private val psi: CommitScopePsiElement
) : CommitLookupElement() {
  override val weight = 20

  override fun getPsiElement() = psi
  override fun getLookupString() = psi.commitScope.text
  override fun renderElement(presentation: LookupElementPresentation) {
    val commitScope = psi.commitScope
    val rendering = commitScope.getRendering()
    presentation.run {
      itemText = commitScope.text
      icon = CCIcons.SCOPE
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
    val (type, _, _, _, subject) = CCParser.parseText(lineText)
    val newTextBuilder = StringBuilder(150)
      .append(type.value)
      .append('(')
      .append(lookupString)
      .append("):")

    val typeStartOffset = lineRange.first + type.range.first
    val newTextLengthWithoutSubject =
      newTextBuilder.length +
      if (!subject.isValid || subject.value.startsWith(" ")) 1 else 0

    val newText = newTextBuilder
      .append(subject.value.ifEmpty { " " })
      .toString()

    runWriteAction {
      document.replaceString(typeStartOffset, lineRange.last, newText)
    }

    editor.caretModel.moveToOffset(typeStartOffset + newTextLengthWithoutSubject)
    AutoPopupController.getInstance(context.project).scheduleAutoPopup(editor)
  }
}
