package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.*
import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.lookupElement.TemplateSegment
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import kotlin.math.max
import kotlin.math.min

/**
 * @author Edoardo Luppi
 */
internal class CCTemplateEditingListener : TemplateEditingAdapter() {
  private companion object {
    private val logger = logger<CCTemplateEditingListener>()
  }

  private var isCancelled = false

  override fun templateCancelled(template: Template) {
    isCancelled = true
  }

  override fun currentVariableChanged(
    templateState: TemplateState,
    template: Template,
    oldIndex: Int,
    newIndex: Int,
  ) {
    if (newIndex < 0) {
      if (isCancelled || templateState.documentChangesTerminateTemplate()) {
        beforeTemplateFinished(templateState, template)
      }

      return
    }

    if (oldIndex == TemplateSegment.BodyOrFooterType && newIndex > oldIndex) {
      if (templateState.getSegmentRange(TemplateSegment.BodyOrFooterType).isEmpty) {
        deleteFooterValue(templateState)
        templateState.gotoEnd()
        return
      }
    }

    val newOffset = templateState.getSegmentRange(newIndex).startOffset
    val editor = templateState.editor
    editor.moveCaretToOffset(newOffset)
    editor.scheduleAutoPopup()
  }

  override fun beforeTemplateFinished(templateState: TemplateState, template: Template) {
    val bodyOrFooterTypeRange = templateState.getSegmentRange(TemplateSegment.BodyOrFooterType)

    if (bodyOrFooterTypeRange.isEmpty) {
      repositionCursorAfterSubjectAndCleanUp(templateState, bodyOrFooterTypeRange)
    }

    deleteScopeParenthesesIfEmpty(templateState)
  }

  private fun repositionCursorAfterSubjectAndCleanUp(
    templateState: TemplateState,
    bodyOrFooterTypeRange: TextRange,
  ) {
    // If the body is empty, it means the user didn't need to insert it.
    // Thus, we can reposition the cursor at the end of the subject
    val newOffset = templateState.getSegmentRange(TemplateSegment.Subject).endOffset

    if (newOffset <= bodyOrFooterTypeRange.endOffset) {
      val editor = templateState.editor
      val action = {
        editor.document.deleteString(newOffset, bodyOrFooterTypeRange.endOffset)
        editor.moveCaretToOffset(newOffset)
      }

      WriteCommandAction.runWriteCommandAction(editor.project, "Reposition cursor after the subject", "", action)
    }
  }

  private fun deleteScopeParenthesesIfEmpty(templateState: TemplateState) {
    val (scopeStart, scopeEnd, isScopeEmpty) = templateState.getSegmentRange(TemplateSegment.Scope)

    // If the scope is empty, it means the user didn't need to insert it, thus we can remove it
    if (isScopeEmpty) {
      val editor = templateState.editor
      val document = editor.document
      val startOffset = max(scopeStart - 1, 0)
      val endOffset = min(scopeEnd + 1, document.textLength)
      val action = {
        document.deleteString(startOffset, endOffset)
      }

      WriteCommandAction.runWriteCommandAction(editor.project, "Delete scope's parentheses", "", action)
    }
  }

  private fun deleteFooterValue(templateState: TemplateState) {
    val (start, end, isEmpty) = templateState.getSegmentRange(TemplateSegment.FooterValue)

    if (!isEmpty) {
      runWriteAction {
        templateState.editor.document.deleteString(start, end)
      }
    }
  }

  @Compatibility(
    keepForHistoricReasons = true,
    minVersion = "202.4357.23",
    description = """
      NOTE: we keep this just to be safe!
      
      On newer IDEA versions the templateCancelled method is called before
      the last currentVariableChanged (with newIndex < 0), so we have the opportunity
      to store a boolean for that.
      On older versions it's the opposite, and this is the only possible way.
      """,
  )
  private fun TemplateState.documentChangesTerminateTemplate(): Boolean =
    logger.runAndLogError(false) {
      TemplateState::class.java.getDeclaredField("myDocumentChangesTerminateTemplate").let {
        it.isAccessible = true
        it.get(this) as Boolean
      }
    }
}
