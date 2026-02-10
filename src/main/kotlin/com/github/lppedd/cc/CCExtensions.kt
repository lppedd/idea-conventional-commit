package com.github.lppedd.cc

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.FileSystemInterface
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.ui.scale.JBUIScale
import java.awt.Color
import java.awt.Robot
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.CancellationException
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.ListSelectionModel
import javax.swing.border.Border
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.InlineOnly
import kotlin.math.max
import kotlin.math.min

// region Project

internal fun Project.findRootDir(): VirtualFile? {
  val baseDirs = getBaseDirectories()

  if (baseDirs.isNotEmpty()) {
    return baseDirs.first()
  }

  // The base path is not prefixed with a file:// schema
  val basePath = this.basePath

  if (basePath != null) {
    return LocalFileSystem.getInstance().findFileByPath(basePath)
  }

  return null
}

// endregion
// region PsiElement

@InlineOnly
internal inline val PsiElement.endOffset
  get() = textRange.endOffset

// endregion
// region Action

@InlineOnly
internal inline fun Action.setName(name: String) {
  putValue(Action.NAME, name)
}

@InlineOnly
internal inline fun Action.setFocused(isFocused: Boolean = true) {
  putValue(DialogWrapper.FOCUSED_ACTION, if (isFocused) true else null)
}

// endregion
// region Presentation

internal fun Presentation.updateIcons(withIcon: Icon) {
  val darkIcon = IconLoader.getDarkIcon(withIcon, true)
  selectedIcon = darkIcon
  hoveredIcon = darkIcon
  icon = withIcon
}

// endregion
// region Color

@Suppress("unused", "UseJBColor")
internal fun Color.darker(factor: Double): Color =
  Color(
    max((red * factor).toInt(), 0),
    max((green * factor).toInt(), 0),
    max((blue * factor).toInt(), 0),
    alpha,
  )

@Suppress("ConvertTwoComparisonsToRangeCheck", "UseJBColor")
internal fun Color.brighter(factor: Double): Color {
  var r = red
  var g = green
  var b = blue
  val alpha = alpha
  val i = (1.0 / (1.0 - factor)).toInt()

  if (r == 0 && g == 0 && b == 0) {
    return Color(i, i, i, alpha)
  }

  if (r > 0 && r < i) r = i
  if (g > 0 && g < i) g = i
  if (b > 0 && b < i) b = i

  return Color(
    min((r / factor).toInt(), 255),
    min((g / factor).toInt(), 255),
    min((b / factor).toInt(), 255),
    alpha,
  )
}

// endregion
// region Border

@InlineOnly
internal inline fun Border.wrap(border: Border): Border =
  BorderFactory.createCompoundBorder(this, border)

// endregion
// region VirtualFile

@InlineOnly
@Suppress("unused")
internal inline val VirtualFile.isHidden: Boolean
  get() = `is`(VFileProperty.HIDDEN)

@InlineOnly
internal inline val VirtualFile.isSymlink: Boolean
  get() = `is`(VFileProperty.SYMLINK)

internal fun VirtualFile.getReliableInputStream(): InputStream {
  val fs = this.fileSystem

  if (fs is FileSystemInterface) {
    return fs.getInputStream(this)
  }

  // Avoid using VirtualFile.getInputStream as it strips the file BOM
  val content = ReadAction.compute<ByteArray, Throwable> {
    contentsToByteArray(/* cacheContent = */ false)
  }

  return ByteArrayInputStream(content)
}

// endregion
// region PsiFile

internal inline val PsiFile.document: Document?
  @InlineOnly
  get() = PsiDocumentManager.getInstance(project).getDocument(this)

// endregion
// region Document

@InlineOnly
internal inline fun Document.getSegment(start: Int, end: Int): CharSequence =
  immutableCharSequence.subSequence(start, end)

internal fun Document.isLineEmpty(line: Int): Boolean =
  getLineStartOffset(line) == getLineEndOffset(line)

internal fun Document.getLineRange(line: Int): TextRange =
  TextRange(getLineStartOffset(line), getLineEndOffset(line))

internal fun Document.getLineRangeByOffset(offset: Int): TextRange =
  getLineRange(getLineNumber(offset))

internal fun Document.getLine(line: Int): CharSequence {
  val (start, end) = getLineRange(line)
  return immutableCharSequence.subSequence(start, end)
}

private val commitMessageDataKey: Key<*>? by lazy {
  try {
    val commitMessageClass = Class.forName("com.intellij.openapi.vcs.ui.CommitMessage")
    val dataKeyField = commitMessageClass.getField("DATA_KEY")
    dataKeyField.get(null) as Key<*>?
  } catch (_: ClassNotFoundException) {
    null
  }
}

internal fun Document.isCommitMessage(): Boolean {
  val dataKey = commitMessageDataKey
  return dataKey != null && getUserData(dataKey) != null
}

// endregion
// region Editor

@InlineOnly
internal inline fun Editor.getCaretOffset(): Int =
  caretModel.offset

@InlineOnly
internal inline fun Editor.scheduleAutoPopup() {
  AutoPopupController.getInstance(project ?: return).scheduleAutoPopup(this)
}

@InlineOnly
internal inline fun Editor.selectAll() {
  selectionModel.setSelection(0, document.textLength)
}

@InlineOnly
internal inline fun Editor.removeSelection(allCarets: Boolean = false) =
  selectionModel.removeSelection(allCarets)

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
internal inline fun Editor.insertStringAtCaret(string: String, moveCaret: Boolean = true) {
  EditorModificationUtil.insertStringAtCaret(this, string, false, moveCaret)
}

internal fun Editor.replaceString(
  startOffset: Int,
  endOffset: Int,
  newString: CharSequence,
  moveCaret: Boolean = true,
) {
  document.replaceString(startOffset, endOffset, newString)

  if (moveCaret) {
    caretModel.moveToOffset(startOffset + newString.length)
  }
}

@InlineOnly
internal inline fun Editor.getCharAfterCaret(): Char? =
  document.immutableCharSequence.getOrNull(caretModel.offset)

@InlineOnly
internal inline fun Editor.getTemplateState(): TemplateState? =
  TemplateManagerImpl.getTemplateState(this)

@InlineOnly
internal inline fun Editor.isTemplateActive(): Boolean =
  getTemplateState()?.isFinished == false

internal fun Editor.getCurrentLineRange(): TextRange {
  val mainCaretLine = EditorUtil.calcCaretLineRange(this).first.line
  return document.getLineRange(mainCaretLine)
}

internal fun Editor.getCurrentLine(): CharSequence {
  val (start, end) = getCurrentLineRange()
  return document.immutableCharSequence.subSequence(start, end)
}

@Suppress("unused")
internal fun Editor.getCurrentLineUntilCaret(): CharSequence {
  val logicalPosition = caretModel.logicalPosition
  val lineStartOffset = document.getLineStartOffset(logicalPosition.line)
  return document.getSegment(lineStartOffset, lineStartOffset + logicalPosition.column)
}

internal fun Editor.getCaretOffsetInLine(): Int {
  val logicalPosition = caretModel.logicalPosition
  val lineStartOffset = document.getLineStartOffset(logicalPosition.line)
  return caretModel.offset - lineStartOffset
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
// region CompletionParameters

@InlineOnly
internal inline fun CompletionParameters.getCompletionPrefix(): String =
  TextFieldWithAutoCompletionListProvider.getCompletionPrefix(this)

// endregion
// region StringBuilder

@InlineOnly
internal inline fun StringBuilder.deleteLast(): StringBuilder =
  deleteCharAt(length - 1)

// endregion
// region CharSequence

internal val WHITESPACE_REGEX: Regex = "\\s+".toRegex()

@InlineOnly
internal inline fun CharSequence.flattenWhitespaces(): String =
  replace(WHITESPACE_REGEX, " ")

internal fun CharSequence.firstIsWhitespace(): Boolean =
  firstOrNull()?.isWhitespace() == true

// endregion
// region String

private val nonThinRegex = Regex("[^iIl1.,']")

@InlineOnly
private inline fun textWidth(str: String): Int =
  str.length - nonThinRegex.replace(str, "").length / 2

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
internal inline operator fun <T, C : MutableList<T>> T.plus(list: C): C {
  list.add(0, this)
  return list
}

// endregion
// region Utilities

@InlineOnly
internal inline fun <T> safeRunWithCheckCanceled(noinline callable: () -> T): T {
  val progressIndicator = ProgressManager.getInstance().progressIndicator
  return if (progressIndicator != null) {
    ApplicationUtil.runWithCheckCanceled(callable, progressIndicator)
  } else {
    callable()
  }
}

@InlineOnly
internal inline fun runInWriteActionIfNeeded(noinline block: () -> Unit) {
  val application = ApplicationManager.getApplication()

  if (application.isWriteAccessAllowed) {
    block()
  } else {
    application.runWriteAction(block)
  }
}

@InlineOnly
internal inline fun runInBackgroundThread(noinline block: () -> Unit) {
  ApplicationManager.getApplication().executeOnPooledThread(block)
}

@InlineOnly
internal inline fun invokeLaterOnEdt(noinline block: () -> Unit) {
  ApplicationManager.getApplication().invokeLater(block)
}

@InlineOnly
internal inline fun invokeLaterOnEdtAndWait(noinline block: () -> Unit) {
  ApplicationManager.getApplication().invokeAndWait(block)
}

@InlineOnly
internal inline fun Any.getResourceAsStream(path: String): InputStream =
  javaClass.getResourceAsStream(path)!!

@InlineOnly
internal inline fun Robot.keyPressAndRelease(keyCode: Int, delay: Int = 0) {
  keyPress(keyCode)

  if (delay > 0) {
    delay(delay)
  }

  keyRelease(keyCode)
}

@InlineOnly
internal inline fun ListSelectionModel.selectedIndices(): IntArray {
  val iMin = minSelectionIndex
  val iMax = maxSelectionIndex

  if (iMin < 0 || iMax < 0) {
    return IntArray(0)
  }

  val rvTmp = IntArray(1 + (iMax - iMin))
  var n = 0

  @Suppress("LoopToCallChain")
  for (i in iMin..iMax) {
    if (isSelectedIndex(i)) {
      rvTmp[n++] = i
    }
  }

  val rv = IntArray(n)
  System.arraycopy(rvTmp, 0, rv, 0, n)

  return rv
}

internal fun <T> Logger.runAndLogError(defaultValue: T, block: () -> T): T {
  contract {
    callsInPlace(block, InvocationKind.AT_MOST_ONCE)
  }

  return try {
    block()
  } catch (e: ProcessCanceledException) {
    throw e
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    error(e)
    defaultValue
  }
}

internal val Int.scaled
  get() = JBUIScale.scale(this)

internal val Float.scaled
  get() = JBUIScale.scale(this)

// endregion
