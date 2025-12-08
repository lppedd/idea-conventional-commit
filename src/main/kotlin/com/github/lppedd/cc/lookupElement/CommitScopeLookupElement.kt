package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitScope
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * Represents an item in the completion's popup inside the scope context.
 *
 * @author Edoardo Luppi
 */
internal class CommitScopeLookupElement(
    private val psiElement: CommitScopePsiElement,
    private val commitScope: CommitScope,
) : CommitTokenLookupElement() {
  override fun getToken(): CommitToken =
    commitScope

  override fun getPsiElement(): CommitScopePsiElement =
    psiElement

  override fun getLookupString(): String =
    commitScope.getValue()

  override fun getItemText(): String =
    commitScope.getText()

  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.icon = CCIcons.Tokens.Scope
    super.renderElement(presentation)
  }

  @Suppress("DuplicatedCode")
  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val (lineStartOffset, lineEndOffset) = editor.getCurrentLineRange()
    val lineText = context.document.getSegment(lineStartOffset, lineEndOffset)
    val (_, scope, breakingChange, separator, subject) = CCParser.parseHeader(lineText)

    check(scope is ValidToken) {
      "The scope token should be valid here. There might be a parser issue"
    }

    // Replace the old scope with the new one
    editor.replaceString(
        lineStartOffset + scope.range.startOffset,
        lineStartOffset + scope.range.endOffset,
        commitScope.getValue(),
    )

    // If a closing scope's paren isn't already present, add it
    if (editor.getCharAfterCaret() != ')') {
      editor.insertStringAtCaret(")", moveCaret = false)
    }

    // Move the caret after the closing paren and breaking change symbol, if present.
    // The condition is inside the parameter space to save a call to moveCaretRelatively
    editor.moveCaretRelatively(if (breakingChange.isPresent) 2 else 1)

    // If a separator isn't already present, add it
    if (!separator.isPresent) {
      editor.insertStringAtCaret(":", moveCaret = false)
    }

    // Move the caret after the separator
    editor.moveCaretRelatively(1)

    // If the subject is present and starts with whitespace,
    // shift the caret of one position, otherwise insert whitespace
    if (subject is ValidToken) {
      if (subject.value.firstIsWhitespace()) {
        editor.moveCaretRelatively(1)
      }
    } else {
      editor.insertStringAtCaret(" ")
    }

    editor.scheduleAutoPopup()
  }
}
