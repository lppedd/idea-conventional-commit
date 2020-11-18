package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.completion.providers.TypeProviderWrapper
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import kotlin.math.max
import kotlin.math.min

internal const val INDEX_TYPE = 0
internal const val INDEX_SCOPE = 1
internal const val INDEX_SUBJECT = 2
internal const val INDEX_BODY_OR_FOOTER_TYPE = 3
internal const val INDEX_FOOTER_VALUE = 4

/**
 * @author Edoardo Luppi
 *
 * @see com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
 * @see com.intellij.codeInsight.template.impl.Variable
 */
internal class TemplateCommitTypeLookupElement(
    index: Int,
    provider: TypeProviderWrapper,
    psiElement: CommitTypePsiElement,
) : CommitTypeLookupElement(index, provider, psiElement) {
  private val templateSettings = TemplateSettings.getInstance()

  /**
   * When the user select the commit type a new template has to be initiated.
   * The template should start with the commit type already inserted, and with the
   * caret positioned in the commit scope context, with an active completion popup.
   */
  override fun handleInsert(context: InsertionContext) {
    val template = templateSettings.getTemplateById("ConventionalCommit-cs") as? TemplateImpl ?: return
    val project = context.project
    val editor = context.editor

    // We delete the text inserted when the user confirmed the commit type (e.g. "build")
    runWriteAction {
      editor.document.deleteString(context.startOffset, context.tailOffset)
    }

    // Now that we have a clean document state, we can initiate the template
    // at the original (pre-commit type insertion) caret position
    TemplateManager.getInstance(project).startTemplate(
      editor,
      template,
      true,
      null,
      CCTemplateEditingListener()
    )

    // We populate the macro type context with the chosen commit type
    runWriteAction {
      editor.insertStringAtCaret(psiElement.commitType.value)
    }

    // ...and we confirm it by navigating to the subject context
    editor.getTemplateState()?.nextTab()
  }
}

private class CCTemplateEditingListener : TemplateEditingAdapter() {
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

    if (oldIndex == INDEX_BODY_OR_FOOTER_TYPE && newIndex > oldIndex) {
      if (templateState.getSegmentRange(INDEX_BODY_OR_FOOTER_TYPE).isEmpty) {
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
    val bodyOrFooterTypeRange = templateState.getSegmentRange(INDEX_BODY_OR_FOOTER_TYPE)

    if (bodyOrFooterTypeRange.isEmpty) {
      repositionCursorAfterSubjectAndCleanUp(templateState, bodyOrFooterTypeRange)
    }

    deleteScopeParenthesesIfEmpty(templateState)
  }

  private fun repositionCursorAfterSubjectAndCleanUp(
      templateState: TemplateState,
      bodyOrFooterTypeRange: TextRange,
  ) {
    // If the body is empty it means the user didn't need to insert it,
    // thus we can reposition the cursor at the end of the subject
    val newOffset = templateState.getSegmentRange(INDEX_SUBJECT).endOffset
    val editor = templateState.editor
    val toDo = Runnable {
      editor.document.deleteString(newOffset, bodyOrFooterTypeRange.endOffset)
      editor.moveCaretToOffset(newOffset)
    }

    runWriteCommandAction(editor.project, "Reposition cursor after subject", "", toDo)
  }

  private fun deleteScopeParenthesesIfEmpty(templateState: TemplateState) {
    val (scopeStart, scopeEnd, isScopeEmpty) = templateState.getSegmentRange(INDEX_SCOPE)

    // If the scope is empty it means the user didn't need to insert it,
    // thus we can remove it
    if (isScopeEmpty) {
      val editor = templateState.editor
      val document = editor.document
      val startOffset = max(scopeStart - 1, 0)
      val endOffset = min(scopeEnd + 1, document.textLength)
      val toDo = Runnable {
        document.deleteString(startOffset, endOffset)
      }

      runWriteCommandAction(editor.project, "Delete scope's parentheses", "", toDo)
    }
  }

  private fun deleteFooterValue(templateState: TemplateState) {
    val (start, end, isEmpty) = templateState.getSegmentRange(INDEX_FOOTER_VALUE)

    if (!isEmpty) {
      runWriteAction {
        templateState.editor.document.deleteString(start, end)
      }
    }
  }

  @Compatibility(
    minVersion = "202.4357.23",
    replaceWith = """
      On newer IDEA versions the templateCancelled method is called before
      the last currentVariableChanged (with newIndex < 0), so we have the opportunity
      to store a boolean for that.
      On older versions it's the opposite and this is the only possible way.
    """
  )
  private fun TemplateState.documentChangesTerminateTemplate(): Boolean =
    logger.runAndLogError(false) {
      TemplateState::class.java.getDeclaredField("myDocumentChangesTerminateTemplate").let {
        it.isAccessible = true
        it.get(this) as Boolean
      }
    }
}
