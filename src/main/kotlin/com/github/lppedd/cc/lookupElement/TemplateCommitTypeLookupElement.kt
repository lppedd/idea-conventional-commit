package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.application.runWriteAction

/**
 * @author Edoardo Luppi
 *
 * @see com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
 * @see com.intellij.codeInsight.template.impl.Variable
 */
internal class TemplateCommitTypeLookupElement(
    index: Int,
    psiElement: CommitTypePsiElement,
) : CommitTypeLookupElement(index, psiElement) {
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
    private val templateSettings = TemplateSettings.getInstance()

    override fun templateFinished(template: Template, brokenOff: Boolean) {
      // TODO: handle "no scope" insertion by deleting the empty parenthesis "()"
    }

    override fun templateCancelled(template: Template?) {
      // TODO: handle "no scope" insertion by deleting the empty parenthesis "()"
    }

    override fun currentVariableChanged(
        templateState: TemplateState,
        template: Template?,
        oldIndex: Int,
        newIndex: Int,
    ) {
      // TODO: maybe remove deleting the empty parenthesis "()" immediately
      //  in favor of doing it on template ending
      if (oldIndex != 1 || newIndex != 2) {
        return
      }

      val (scopeStart, scopeEnd, isEmpty) = templateState.getSegmentRange(1)

      if (!isEmpty) {
        return
      }

      val editor = templateState.editor

      runWriteAction {
        editor.document.deleteString(scopeStart - 1, scopeEnd + 1)
      }

      TemplateManager.getInstance(editor.project).startTemplate(
        editor,
        templateSettings.getTemplateById("ConventionalCommit-subject"),
        true,
        null,
        this
      )
    }
  }
}
