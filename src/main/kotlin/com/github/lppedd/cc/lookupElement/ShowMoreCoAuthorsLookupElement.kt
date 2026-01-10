package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitFooterValue
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.api.TokenPresentation
import com.github.lppedd.cc.configuration.CCTokensService
import com.github.lppedd.cc.configuration.CoAuthorsResult
import com.github.lppedd.cc.configuration.component.CoAuthorsDialog
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.impl.PrefixChangeListener
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager

/**
 * @author Edoardo Luppi
 */
internal class ShowMoreCoAuthorsLookupElement : CommitTokenLookupElement, PrefixChangeListener {
  private companion object {
    private val logger = logger<ShowMoreCoAuthorsLookupElement>()
  }

  private var userInsertedText: StringBuilder
  private val psiElement: CommitFooterValuePsiElement

  constructor(project: Project, completionPrefix: String) : super() {
    userInsertedText = StringBuilder(50).append(completionPrefix)

    val token = ShowMoreCoAuthorsCommitFooterValue
    psiElement = CommitFooterValuePsiElement(project, token.getValue())
  }

  override fun beforeAppend(char: Char) {
    userInsertedText.append(char)
  }

  override fun beforeTruncate() {
    userInsertedText.deleteLast()
  }

  override fun getToken(): CommitToken =
    ShowMoreCoAuthorsCommitFooterValue

  override fun getPsiElement(): CommitFooterValuePsiElement =
    psiElement

  override fun getLookupString(): String =
    "${getToken().getValue()}$userInsertedText"

  override fun getItemText(): String =
    lookupString

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.itemText = getToken().getText()
    presentation.isTypeIconRightAligned = true
    presentation.isItemTextBold = true
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
    val project = context.project
    val tokensService = CCTokensService.getInstance(project)
    val coAuthors = when (val result = tokensService.getCoAuthors()) {
      is CoAuthorsResult.Success -> result.coAuthors
      is CoAuthorsResult.Failure -> {
        logger.error("Error while getting co-authors", result.message)
        emptySet()
      }
    }

    val dialog = CoAuthorsDialog(project, coAuthors)

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

    val action = Runnable {
      editor.removeSelection()
      editor.replaceString(footerStart, footerEnd, text)
    }

    WriteCommandAction.runWriteCommandAction(
      project,
      commandName,
      commandGroupId,
      action,
      PsiDocumentManager.getInstance(project).getPsiFile(document),
    )
  }

  private object ShowMoreCoAuthorsCommitFooterValue : CommitFooterValue {
    override fun getText(): String =
      getValue()

    override fun getValue(): String =
      CCBundle["cc.completion.showMore"]

    override fun getDescription(): String =
      CCBundle["cc.config.coAuthors.description"]

    override fun getPresentation(): TokenPresentation =
      object : TokenPresentation {}
  }
}
