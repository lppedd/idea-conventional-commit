package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.ICON_TYPE
import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.getCurrentLineRange
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.runWriteAction

/**
 * @author Edoardo Luppi
 */
internal open class CommitTypeLookupElement(
    override val index: Int,
    private val psi: CommitTypePsiElement,
) : CommitLookupElement() {
  override val weight = 30
  override fun getPsiElement() = psi
  override fun getLookupString() = psi.commitType.text

  override fun renderElement(presentation: LookupElementPresentation) {
    val commitType = psi.commitType
    val rendering = commitType.getRendering()
    presentation.itemText = commitType.text
    presentation.icon = ICON_TYPE
    presentation.isItemTextBold = rendering.bold
    presentation.isItemTextItalic = rendering.italic
    presentation.isStrikeout = rendering.strikeout
    presentation.isTypeIconRightAligned = true
    presentation.setTypeText(rendering.type, rendering.icon)
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document

    val (_, lineEnd) = editor.getCurrentLineRange()
    val lineText = document.charsSequence.subSequence(context.tailOffset, lineEnd)

    if (lineText.isNotEmpty()) {
      val firstSpaceIndex =
        lineText.indexOfAny(charArrayOf('(', ':'))
          .let { if (it > 0) it else lineText.lastIndex }
          .let(lineText::take)
          .indexOf(' ')

      if (firstSpaceIndex >= 0) {
        runWriteAction {
          document.replaceString(context.startOffset, context.tailOffset + firstSpaceIndex, lookupString)
        }

        return
      }
    }

    val type = CCParser.parseText(lineText).type

    if (type is ValidToken) {
      val range = type.range
      val text = lineText.replaceRange(range.first, range.last + 1, lookupString)

      runWriteAction {
        document.replaceString(context.startOffset, lineEnd, text)
      }
    }
  }
}
