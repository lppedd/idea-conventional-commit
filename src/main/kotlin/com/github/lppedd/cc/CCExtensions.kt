package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.ui.scale.JBUIScale
import java.awt.Color
import java.awt.Robot
import java.io.InputStream
import java.util.concurrent.CancellationException
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.ListSelectionModel
import javax.swing.border.Border
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.internal.InlineOnly
import kotlin.math.max
import kotlin.math.min

// region LookupImpl

@Compatibility(
    minVersion = "193.5096.12",
    replaceWith = "LookupImpl#setLookupFocusDegree(LookupFocusDegree)",
)
internal fun LookupImpl.setLookupFocusDegree(focusDegree: String) {
  // Unfortunately this is required to maintain compatibility with versions prior to 193.5096.
  // setLookupFocusDegree and LookupFocusDegree don't exist in those versions.
  val (className, methodName) = if (ApplicationInfo.getInstance().majorVersion.toInt() < 2020) {
    "com.intellij.codeInsight.lookup.impl.LookupImpl\$FocusDegree" to "setFocusDegree"
  } else {
    "com.intellij.codeInsight.lookup.LookupFocusDegree" to "setLookupFocusDegree"
  }

  @Suppress("unchecked_cast")
  val enumClass = Class.forName(className) as Class<out Enum<*>?>
  val enumValue = java.lang.Enum.valueOf(enumClass, focusDegree)
  LookupImpl::class.java.getDeclaredMethod(methodName, enumClass).also {
    it.isAccessible = true
    it.invoke(this, enumValue)
  }
}

// endregion
// region Action

@InlineOnly
internal inline fun Action.setName(name: String) {
  putValue("Name", name)
}

@InlineOnly
internal inline fun Action.setFocused(focused: Boolean = true) {
  putValue(DialogWrapper.FOCUSED_ACTION, focused)
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

@Suppress("unused")
internal fun Color.darker(factor: Double): Color =
  Color(
      max((red * factor).toInt(), 0),
      max((green * factor).toInt(), 0),
      max((blue * factor).toInt(), 0),
      alpha
  )

@Suppress("ConvertTwoComparisonsToRangeCheck")
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
      alpha
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

internal fun Document.isCommitMessage(): Boolean =
  getUserData(CommitMessage.DATA_KEY) != null

// endregion
// region Editor

@InlineOnly
internal inline fun Editor.scheduleAutoPopup() {
  AutoPopupController.getInstance(project ?: return).scheduleAutoPopup(this)
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
internal inline operator fun <T, C : MutableList<T>> T.plus(list: C): C {
  list.add(0, this)
  return list
}

// endregion
// region Sequence

@InlineOnly
internal inline fun Sequence<String>.mapToLowerCase(): Sequence<String> =
  map(String::toLowerCase)

@InlineOnly
internal inline fun Sequence<String>.filterNotEmpty(): Sequence<String> =
  filterNot(String::isEmpty)

@InlineOnly
internal inline fun Sequence<String>.filterNotBlank(): Sequence<String> =
  filterNot(String::isBlank)

@InlineOnly
internal inline fun Sequence<String>.trim(): Sequence<String> =
  map(String::trim)

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

internal fun Throwable.readableMessage(): String =
  if (localizedMessage?.isNotBlank() == true) {
    localizedMessage
  } else {
    this::class.simpleName ?: "Anonymous exception object"
  }

@Suppress("SameParameterValue")
internal inline fun <T> Logger.runAndLogError(defaultValue: T, block: () -> T): T {
  contract {
    callsInPlace(block, EXACTLY_ONCE)
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
