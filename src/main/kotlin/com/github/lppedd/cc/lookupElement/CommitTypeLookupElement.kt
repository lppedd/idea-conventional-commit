package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.parser.isInContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal open class CommitTypeLookupElement(
    override val index: Int,
    private val psiElement: CommitTypePsiElement,
) : CommitLookupElement() {
  override val weight: UInt = WEIGHT_TYPE
  override val sourceProviderId: String = psiElement.ownedBy

  override fun getPsiElement(): CommitTypePsiElement =
    psiElement

  override fun getLookupString(): String =
    psiElement.commitType.value

  override fun renderElement(presentation: LookupElementPresentation) {
    val commitType = psiElement.commitType
    val rendering = commitType.getRendering()
    presentation.itemText = commitType.value
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

    val (lineStart, lineEnd) = editor.getCurrentLineRange()
    val documentText = document.immutableCharSequence
    val lineText = documentText.subSequence(lineStart, lineEnd)
    val type = CCParser.parseHeader(lineText).type
    val startingCaretOffset = context.tailOffset - lineStart - lookupString.length

    if (type is ValidToken && type.isInContext(startingCaretOffset)) {
      if (type.value != lookupString) {
        val text = type.range.replace("$lineText", lookupString)
        document.replaceString(lineStart, lineEnd, text)
      }
    } else {
      val startOffset = context.startOffset
      document.replaceString(
        startOffset,
        maxOf(documentText.indexOf(' ', startOffset), 0),
        lookupString
      )
    }
  }
}
