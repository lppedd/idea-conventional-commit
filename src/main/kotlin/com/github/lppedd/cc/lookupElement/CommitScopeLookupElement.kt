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
    val range = CCEditorUtils.getCurrentLineRange(editor)
    val text = document.text.substring(range.first, range.last - 1)
    val commitTokens = CCParser.parseText(text)
    val newTextBuilder = StringBuilder(150)
      .append(commitTokens.type.value)
      .append('(')
      .append(lookupString)
      .append("):")

    val length = newTextBuilder.length
    val newText = newTextBuilder
      .append(commitTokens.subject.value.ifEmpty { " " })
      .toString()

    val typeRangeStart = range.first + commitTokens.type.range.first
    val newOffset = typeRangeStart + length + 1

    runWriteAction {
      document.replaceString(typeRangeStart, range.last, newText)
    }

    editor.caretModel.moveToOffset(newOffset)
    AutoPopupController.getInstance(context.project).scheduleAutoPopup(editor)
  }
}
