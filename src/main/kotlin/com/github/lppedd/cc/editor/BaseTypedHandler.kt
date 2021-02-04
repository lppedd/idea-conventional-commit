package com.github.lppedd.cc.editor

import com.github.lppedd.cc.getCurrentLine
import com.github.lppedd.cc.isCommitMessage
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * @author Edoardo Luppi
 */
internal abstract class BaseTypedHandler(protected val myChar: Char) : TypedHandlerDelegate() {
  final override fun beforeCharTyped(
      ch: Char,
      project: Project,
      editor: Editor,
      file: PsiFile,
      fileType: FileType,
  ): Result {
    return if (myChar == ch) {
      beforeCharTyped(getCommitTokensOrNull(editor) ?: return CONTINUE, editor)
    } else {
      CONTINUE
    }
  }

  final override fun checkAutoPopup(
      ch: Char,
      project: Project,
      editor: Editor,
      file: PsiFile,
  ): Result {
    return if (myChar == ch) {
      checkAutoPopup(getCommitTokensOrNull(editor) ?: return CONTINUE, editor)
    } else {
      CONTINUE
    }
  }

  abstract fun beforeCharTyped(commitTokens: CommitTokens, editor: Editor): Result
  abstract fun checkAutoPopup(commitTokens: CommitTokens, editor: Editor): Result

  private fun getCommitTokensOrNull(editor: Editor): CommitTokens? {
    if (!editor.document.isCommitMessage()) {
      return null
    }

    val line = editor.getCurrentLine().trimEnd()
    val commitTokens = CCParser.parseHeader(line)
    return if (commitTokens.type is ValidToken) commitTokens else null
  }
}
