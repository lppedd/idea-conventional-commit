package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitFooterValue
import com.github.lppedd.cc.completion.providers.FakeProviderWrapper
import com.github.lppedd.cc.configuration.component.CoAuthorsDialog
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager

/**
 * @author Edoardo Luppi
 */
internal class ShowMoreCoAuthorsLookupElement : CommitLookupElement, PrefixChangeListener {
  @Suppress("JoinDeclarationAndAssignment")
  private var userInsertedText: StringBuilder
  private val psiElement: CommitFooterValuePsiElement

  constructor(project: Project, completionPrefix: String)
      : super(2000, CC.Tokens.PriorityFooterValue, FakeProviderWrapper) {
    userInsertedText = StringBuilder(50).append(completionPrefix)
    psiElement = CommitFooterValuePsiElement(
        project,
        CommitFooterValue("", CCBundle["cc.config.coAuthors.description"])
    )
  }

  override fun beforeAppend(char: Char) {
    userInsertedText.append(char)
  }

  override fun beforeTruncate() {
    userInsertedText.deleteLast()
  }

  override fun getPsiElement(): CommitFooterValuePsiElement =
    psiElement

  override fun getLookupString(): String =
    "${CCBundle["cc.completion.showMore"]}$userInsertedText"

  override fun getDisplayedText(): String =
    lookupString

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.also {
      it.itemText = CCBundle["cc.completion.showMore"]
      it.isTypeIconRightAligned = true
      it.isItemTextBold = true
    }
  }

  override fun handleInsert(context: InsertionContext) {
    val startOffset = context.startOffset
    val commandProcessor = CommandProcessor.getInstance()
    val commandGroupId = commandProcessor.currentCommandGroupId as? String
    val commandName = commandProcessor.currentCommandName

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

    val text = dialog.getSelectedAuthors()
      .ifEmpty { return }
      .joinToString("") { "Co-authored-by: ${it.trim()}\n" }
      .dropLast(1)

    val editor = context.editor
    val document = editor.document
    val (lineStart, lineEnd) = document.getLineRangeByOffset(startOffset)
    val footerText = document.getSegment(lineStart, document.textLength)
    val (footerType, _, footerValue) = CCParser.parseFooter(footerText)
    val (footerStart, footerEnd) = if (footerType is ValidToken && footerValue is ValidToken) {
      TextRange(lineStart + footerType.range.startOffset, lineStart + footerValue.range.endOffset)
    } else {
      TextRange(lineStart, lineEnd)
    }

    val runnable = Runnable {
      context.editor.removeSelection()
      editor.replaceString(footerStart, footerEnd, text)
    }

    WriteCommandAction.runWriteCommandAction(
        context.project,
        commandName,
        commandGroupId,
        runnable,
        PsiDocumentManager.getInstance(context.project).getPsiFile(document),
    )
  }
}
