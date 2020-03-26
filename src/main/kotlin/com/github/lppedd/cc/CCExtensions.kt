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
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Couple
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
// region Document

internal fun Document.getLineRange(line: Int): TextRange {
  val lineStartOffset = getLineStartOffset(line)
  val lineEndOffset = getLineEndOffset(line)
  return TextRange(lineStartOffset, lineEndOffset)
}

@InlineOnly
internal inline fun Document.replaceString(range: TextRange, text: CharSequence) {
  replaceString(range.startOffset, range.endOffset, text)
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
internal inline fun Editor.moveCaretRelatively(caretShift: Int) {
  EditorModificationUtil.moveCaretRelatively(this, caretShift)
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

internal fun Editor.getCurrentLineUntilCaret(): CharSequence {
  val logicalPosition = caretModel.logicalPosition
  val lineStartOffset = document.getLineStartOffset(logicalPosition.line)
  return document.getText(lineStartOffset to lineStartOffset + logicalPosition.column)
}

// endregion
// region Couple

@InlineOnly
internal inline operator fun <T> Couple<T>.component1(): T? = first

@InlineOnly
internal inline operator fun <T> Couple<T>.component2(): T? = second

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

internal fun CharSequence?.orWhitespace(): String =
  if (this == null || isEmpty()) " " else this.toString()

internal fun CharSequence.isFirstWhitespace(): Boolean =
  firstOrNull()?.isWhitespace() == true

// endregion
// region Int

@InlineOnly
internal inline infix fun Int.to(that: Int): TextRange = TextRange(this, that)

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
