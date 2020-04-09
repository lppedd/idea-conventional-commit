package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
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
import kotlin.math.max
import kotlin.math.min

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
   * In autopopup context (see `CompletionAutoPopupHandler`),
   * avoid hiding the commit type when it matches entirely what the user typed.
   */
  override fun isWorthShowingInAutoPopup() = true

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
      CCTemplateEditingListener
    )

    // We populate the macro type context with the chosen commit type
    runWriteAction {
      editor.insertStringAtCaret(psiElement.commitType.value)
    }

    // ...and we confirm it by navigating to the subject context
    editor.getTemplateState()?.nextTab()
  }

  object CCTemplateEditingListener : TemplateEditingAdapter() {
    override fun beforeTemplateFinished(templateState: TemplateState, template: Template) {
      val lastSegmentIndex = templateState.segmentsCount - 1
      val currentSegmentIndex = templateState.currentVariableNumber

      if (currentSegmentIndex == lastSegmentIndex) {
        repositionCursorAfterSubjectIfNeeded(templateState, lastSegmentIndex)
      }

      if (currentSegmentIndex > 0) {
        deleteScopeParenthesesIfEmpty(templateState)
      }
    }

    private fun repositionCursorAfterSubjectIfNeeded(templateState: TemplateState, lastSegmentIndex: Int) {
      val (_, bodyEnd, isBodyEmpty) = templateState.getSegmentRange(lastSegmentIndex)

      // If the body is empty it means the user didn't need to insert it,
      // thus we can reposition the cursor at the end of the subject
      if (isBodyEmpty) {
        val newOffset = templateState.getSegmentRange(lastSegmentIndex - 1).endOffset
        val editor = templateState.editor

        runWriteAction {
          editor.document.deleteString(newOffset, bodyEnd)
          editor.moveCaretToOffset(newOffset)
        }
      }
    }

    private fun deleteScopeParenthesesIfEmpty(templateState: TemplateState) {
      val (scopeStart, scopeEnd, isScopeEmpty) = templateState.getSegmentRange(1)

      // If the scope is empty it means the user didn't need to insert it,
      // thus we can remove it
      if (isScopeEmpty) {
        val document = templateState.editor.document
        val startOffset = max(scopeStart - 1, 0)
        val endOffset = min(scopeEnd + 1, document.textLength)

        runWriteAction {
          document.deleteString(startOffset, endOffset)
        }
      }
    }
  }
}
