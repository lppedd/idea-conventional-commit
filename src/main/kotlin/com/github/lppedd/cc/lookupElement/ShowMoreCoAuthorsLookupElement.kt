package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.FooterValueProviderWrapper
import com.github.lppedd.cc.configuration.component.CoAuthorsDialog
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import kotlin.test.assertNotNull

/**
 * @author Edoardo Luppi
 */
internal class ShowMoreCoAuthorsLookupElement(
    index: Int,
    provider: FooterValueProviderWrapper,
    private val psiElement: CommitFooterValuePsiElement,
    completionPrefix: String,
) : CommitLookupElement(index, PRIORITY_FOOTER_VALUE, provider), PrefixChangeListener {
  private var changingLookupString = StringBuilder(50) + completionPrefix

  override fun beforeAppend(ch: Char) {
    changingLookupString += ch
  }

  override fun getPsiElement(): CommitFooterValuePsiElement =
    psiElement

  override fun getLookupString(): String =
    "${CCBundle["cc.completion.showMore"]}$changingLookupString"

  override fun renderElement(presentation: LookupElementPresentation) =
    presentation.let {
      it.itemText = CCBundle["cc.completion.showMore"]
      it.isTypeIconRightAligned = true
      it.isItemTextBold = true
    }

  override fun handleInsert(context: InsertionContext) {
    val commandProcessor = CommandProcessor.getInstance()
    val commandGroupId = commandProcessor.currentCommandGroupId as? String
    val commandName = commandProcessor.currentCommandName

    val document = context.document
    val startOffset = context.startOffset
    val removeTo = context.tailOffset
    val removeFrom = removeTo - lookupString.length
    document.deleteString(removeFrom, removeTo - changingLookupString.length)

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
    val footerText = document.getSegment(lineStart, document.textLength)

    val tokens = CCParser.parseFooter(footerText)
    val footerType = tokens.type
    val footer = tokens.footer
    val (footerStart, footerEnd) =
      if (footerType is ValidToken && footer is ValidToken) {
        TextRange(lineStart + footerType.range.startOffset, lineStart + footer.range.endOffset)
      } else {
        TextRange(lineStart, lineEnd)
      }

    val text = dialog.getSelectedAuthors()
      .ifEmpty { return }
      .joinToString("") { "Co-authored-by: ${it.trim()}\n" }
      .dropLast(1)

    val psiFile = assertNotNull(PsiDocumentManager.getInstance(context.project).getPsiFile(document))
    val toDo = Runnable {
      document.replaceString(footerStart, footerEnd, text)
      editor.moveCaretToOffset(footerStart + text.length)
    }

    WriteCommandAction.runWriteCommandAction(
      context.project,
      commandName,
      commandGroupId,
      toDo,
      psiFile
    )
  }
}
