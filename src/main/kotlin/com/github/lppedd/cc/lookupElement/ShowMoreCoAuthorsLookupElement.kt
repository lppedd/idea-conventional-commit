package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.configuration.component.CoAuthorsDialog
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager

private const val VALUE = "Show more"

/**
 * @author Edoardo Luppi
 */
internal class ShowMoreCoAuthorsLookupElement(
    override val index: Int,
    private val psiElement: CommitFooterPsiElement,
    completionPrefix: String,
) : CommitLookupElement(), PrefixChangeListener {
  private var changingLookupString = StringBuilder(50) + completionPrefix
  override val weight: UInt = WEIGHT_FOOTER

  override fun beforeAppend(ch: Char) {
    changingLookupString += ch
  }

  override fun getPsiElement(): CommitFooterPsiElement =
    psiElement

  override fun getLookupString(): String =
    "$VALUE$changingLookupString"

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = ICON_FOOTER
    presentation.itemText = VALUE
    presentation.isTypeIconRightAligned = true
    presentation.isItemTextBold = true
  }

  override fun handleInsert(context: InsertionContext) {
    val commandProcessor = CommandProcessor.getInstance()
    val commandGroupId = commandProcessor.currentCommandGroupId as? String
    val commandName = commandProcessor.currentCommandName

    val document = context.document
    val startOffset = context.startOffset
    val removeTo = context.tailOffset
    val removeFrom = removeTo - lookupString.length
    document.removeRange(removeFrom, removeTo - changingLookupString.length)

    invokeLaterOnEdt {
      handleCoAuthors(context, startOffset, commandGroupId, commandName)
    }
  }

  private fun handleCoAuthors(
      context: InsertionContext,
      startOffset: Int,
      commandGroupId: String?,
      commandName: String?,
  ) {
    val dialog = CoAuthorsDialog(context.project)

    if (!dialog.showAndGet()) {
      return
    }

    val editor = context.editor
    val document = context.document

    val (lineStart, lineEnd) = document.getLineRangeByOffset(startOffset)
    val footerText = document.getSegment(lineStart until document.textLength)

    val tokens = CCParser.parseFooter(footerText)
    val footerType = tokens.type
    val footer = tokens.footer
    val footerRange = if (footerType is ValidToken && footer is ValidToken) {
      val start = footerType.range.first
      val end = footer.range.last
      lineStart + start until lineStart + end
    } else {
      lineStart until lineEnd
    }

    val text = dialog.getSelectedAuthors()
      .ifEmpty { return }
      .joinToString("") { "Co-authored-by: ${it.trim()}\n" }
      .dropLast(1)

    val psiFile = PsiDocumentManager.getInstance(context.project).getPsiFile(document)
    val toDo = {
      document.replaceString(footerRange.first, footerRange.last + 1, text)
      editor.moveCaretToOffset(footerRange.first + text.length)
    }

    WriteCommandAction.runWriteCommandAction(context.project, commandName, commandGroupId, toDo, psiFile)
  }
}
