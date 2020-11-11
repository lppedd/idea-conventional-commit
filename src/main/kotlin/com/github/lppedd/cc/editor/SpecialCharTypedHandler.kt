package com.github.lppedd.cc.editor

import com.github.lppedd.cc.getCaretOffsetInLine
import com.github.lppedd.cc.getCurrentLine
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.isInContext
import com.github.lppedd.cc.scheduleAutoPopup
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiFile

/**
 * Allows autopopup even when the user types a subset of characters
 * banned by [CompletionAutoPopupHandler].
 *
 * @author Edoardo Luppi
 */
private class SpecialCharTypedHandler : TypedHandlerDelegate() {
  private val allowedSpecialChars = arrayOf('(', ')', '[', ']', '#', '@')

  override fun checkAutoPopup(
      char: Char,
      project: Project,
      editor: Editor,
      file: PsiFile,
  ): Result {
    val document = editor.document
    val lineNumber = document.getLineNumber(editor.caretModel.offset)

    if (!isHandlerApplicable(char, document, lineNumber)) {
      return Result.CONTINUE
    }

    return if (lineNumber == 0) {
      checkHeaderContext(editor)
    } else {
      checkBodyAndFooterContext(editor)
    }
  }

  private fun isHandlerApplicable(char: Char, document: Document, lineNumber: Int): Boolean =
    char in allowedSpecialChars &&

    // This handler is valid only in the context of the commit message
    document.getUserData(CommitMessage.DATA_KEY) != null &&

    // It doesn't make sense to do any processing on a document
    // containing only our special character
    document.textLength != 1 &&

    // The second line (here zero-based) in the document is incompatible with the standard,
    // which requires a blank line between the header (first line)
    // and the body or footer (third line)
    lineNumber != 1

  private fun checkBodyAndFooterContext(editor: Editor): Result {
    println()
    return Result.CONTINUE
  }

  private fun checkHeaderContext(editor: Editor): Result {
    val subject = CCParser.parseHeader(editor.getCurrentLine()).subject
    val caretOffsetInLine = editor.getCaretOffsetInLine()

    if (subject.isInContext(caretOffsetInLine)) {
      editor.scheduleAutoPopup()
      return Result.STOP
    }

    return Result.CONTINUE
  }
}
