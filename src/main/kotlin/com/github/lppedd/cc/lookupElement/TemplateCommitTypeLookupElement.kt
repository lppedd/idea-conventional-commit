package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.api.CommitType
import com.github.lppedd.cc.getTemplateState
import com.github.lppedd.cc.insertStringAtCaret
import com.github.lppedd.cc.liveTemplate.CCTemplateEditingListener
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.application.runWriteAction

// TODO: move them inside an object/namespace
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
  psiElement: CommitTypePsiElement,
  commitType: CommitType,
) : CommitTypeLookupElement(psiElement, commitType) {
  private val templateSettings = TemplateSettings.getInstance()

  /**
   * When the user selects the commit type, a new template has to be initiated.
   * The template should start with the commit type already inserted and with the
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
      CCTemplateEditingListener(),
    )

    // We populate the macro type context with the chosen commit type
    runWriteAction {
      editor.insertStringAtCaret(getToken().getValue())
    }

    // ...and we confirm it by navigating to the subject context
    editor.getTemplateState()?.nextTab()
  }
}
