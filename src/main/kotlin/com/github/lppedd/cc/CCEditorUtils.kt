package com.github.lppedd.cc

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.util.component1
import com.intellij.util.component2

/**
 * @author Edoardo Luppi
 */
internal object CCEditorUtils {
  fun getCurrentLineUntilOffset(editor: Editor, offset: Int): String {
    val document = editor.document
    val lineNumber = document.getLineNumber(offset)
    val lineStartOffset = document.getLineStartOffset(lineNumber)
    return document.text.substring(lineStartOffset, offset)
  }

  fun getCurrentLineUntilCaret(editor: Editor): String {
    val document = editor.document
    val logicalPosition = editor.caretModel.logicalPosition
    val lineStartOffset = document.getLineStartOffset(logicalPosition.line)
    return document.text.substring(lineStartOffset, lineStartOffset + logicalPosition.column)
  }

  fun getCurrentLine(editor: Editor): String {
    val currentLineRange = getCurrentLineRange(editor)
    return editor.document.text.substring(currentLineRange.first, currentLineRange.last)
  }

  fun getCurrentLineRange(editor: Editor): IntRange {
    val (first, _) = EditorUtil.calcCaretLineRange(editor)
    return getLineRange(editor.document, first.line)
  }

  fun getLineRange(document: Document, line: Int): IntRange {
    val lineStartOffset = document.getLineStartOffset(line)
    val lineEndOffset = document.getLineEndOffset(line)
    return IntRange(lineStartOffset, lineEndOffset)
  }
}
