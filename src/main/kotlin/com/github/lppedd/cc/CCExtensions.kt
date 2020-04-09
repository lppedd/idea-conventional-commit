package com.github.lppedd.cc

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import java.io.InputStream
import kotlin.internal.InlineOnly

// region PsiFile

internal inline val PsiFile.document: Document?
  @InlineOnly
  get() = PsiDocumentManager.getInstance(project).getDocument(this)

// endregion
// region LogicalPosition

@InlineOnly
internal inline operator fun LogicalPosition.component1() = line

@InlineOnly
internal inline operator fun LogicalPosition.component2() = column

// endregion
// region Document

@InlineOnly
internal inline fun Document.getSegment(start: Int, end: Int): CharSequence =
  immutableCharSequence.subSequence(start, end)

internal fun Document.getLineRange(line: Int): TextRange =
  TextRange(getLineStartOffset(line), getLineEndOffset(line))

internal fun Document.getLineRangeByOffset(offset: Int): TextRange =
  getLineRange(getLineNumber(offset))

internal fun Document.getLine(line: Int): CharSequence {
  val (start, end) = getLineRange(line)
  return immutableCharSequence.subSequence(start, end)
}

// endregion
// region Editor

@InlineOnly
internal inline fun Editor.scheduleAutoPopup() {
  AutoPopupController.getInstance(project).scheduleAutoPopup(this)
}

@InlineOnly
internal inline fun Editor.moveCaretToOffset(offset: Int, locateBeforeSoftWrap: Boolean = false) {
  caretModel.moveToOffset(offset, locateBeforeSoftWrap)
}

@InlineOnly
internal inline fun Editor.moveCaretRelatively(caretShift: Int): Int {
  EditorModificationUtil.moveCaretRelatively(this, caretShift)
  return caretModel.offset
}

@InlineOnly
@Suppress("RedundantNotNullExtensionReceiverOfInline")
internal inline fun Editor.insertStringAtCaret(string: String) {
  EditorModificationUtil.insertStringAtCaret(this, string)
}

@InlineOnly
internal inline fun Editor.getTemplateState(): TemplateState? =
  TemplateManagerImpl.getTemplateState(this)

internal fun Editor.getCurrentLineRange(): TextRange {
  val mainCaretLine = EditorUtil.calcCaretLineRange(this).first.line
  return document.getLineRange(mainCaretLine)
}

internal fun Editor.getCurrentLine(): CharSequence {
  val (start, end) = getCurrentLineRange()
  return document.immutableCharSequence.subSequence(start, end)
}

internal fun Editor.getCurrentLineUntilCaret(): CharSequence {
  val logicalPosition = caretModel.logicalPosition
  val lineStartOffset = document.getLineStartOffset(logicalPosition.line)
  return document.getSegment(lineStartOffset, lineStartOffset + logicalPosition.column)
}

// endregion
// region TextRange

@InlineOnly
internal inline operator fun TextRange.component1(): Int = startOffset

@InlineOnly
internal inline operator fun TextRange.component2(): Int = endOffset

@InlineOnly
internal inline operator fun TextRange.component3(): Boolean = isEmpty

// endregion
// region LookupImpl

@InlineOnly
internal inline fun LookupImpl.doWhileCalculating(block: () -> Unit) {
  isCalculating = true
  block()
  isCalculating = false
}

// endregion
// region CompletionParameters

@InlineOnly
@Suppress("RedundantNotNullExtensionReceiverOfInline")
internal inline fun CompletionParameters.getCompletionPrefix(): String =
  TextFieldWithAutoCompletionListProvider.getCompletionPrefix(this)

// endregion
// region StringBuilder

@InlineOnly
internal inline operator fun StringBuilder.plus(char: Char): StringBuilder =
  append(char)

@InlineOnly
internal inline operator fun StringBuilder.plus(string: String): StringBuilder =
  append(string)

@InlineOnly
internal inline operator fun StringBuilder.plusAssign(string: String) {
  append(string)
}

// endregion
// region CharSequence

internal val WHITESPACE_REGEX: Regex = "\\s+".toRegex()

@InlineOnly
internal inline fun CharSequence.flattenWhitespaces(): String =
  replace(WHITESPACE_REGEX, " ")

internal fun CharSequence?.orWhitespace(): String =
  if (this == null || isEmpty()) " " else this.toString()

internal fun CharSequence.firstIsWhitespace(): Boolean =
  firstOrNull()?.isWhitespace() == true

// endregion
// region String

private val NON_THIN_REGEX = Regex("[^iIl1.,']")

@InlineOnly
private inline fun textWidth(str: String): Int =
  str.length - NON_THIN_REGEX.replace(str, "").length / 2

// Adapted from https://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis
internal fun String.abbreviate(max: Int, suffix: CharSequence = "..."): String {
  if (textWidth(this) <= max) {
    return this
  }

  var end = minOf(
    lastIndexOf(' ', max - 3),
    lastIndexOf('\n', max - 3),
    lastIndexOf('\r', max - 3),
  )

  if (end == -1) {
    return take(max - 3) + suffix
  }

  var newEnd = end

  do {
    end = newEnd
    newEnd = minOf(
      indexOf(' ', end + 1),
      indexOf('\n', end + 1),
      indexOf('\r', end + 1),
    )

    if (newEnd == -1) {
      newEnd = length
    }
  } while (textWidth(substring(0, newEnd) + suffix) < max)

  return take(end) + suffix
}

// endregion
// region Collections

@InlineOnly
internal inline fun <T> emptyCollection(): Collection<T> =
  emptyList()

// endregion
// region Utilities

@InlineOnly
internal inline fun <T> runWithCheckCanceled(noinline callable: () -> T): T =
  ApplicationUtil.runWithCheckCanceled(callable, ProgressManager.getInstance().progressIndicator)

@InlineOnly
internal inline fun invokeLaterOnEdt(noinline block: () -> Unit) {
  ApplicationManager.getApplication().invokeLater(block)
}

@InlineOnly
internal inline fun Any.getResourceAsStream(path: String): InputStream =
  javaClass.getResourceAsStream(path)!!

// endregion
