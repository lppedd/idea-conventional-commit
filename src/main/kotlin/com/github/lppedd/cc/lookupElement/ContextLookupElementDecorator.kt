package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.psiElement.CommitFakePsiElement
import com.github.lppedd.cc.removeSelection
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase.DIRECT_INSERTION
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.runWriteAction
import com.intellij.psi.PsiDocumentManager

/**
 * Decorates every [CommitLookupElement] when standard code completion is used
 * to provide a stripped-down implementation of the full lookup element
 * insertion cycle (when [DIRECT_INSERTION] is not set).
 *
 * @author Edoardo Luppi
 */
internal class ContextLookupElementDecorator : CommitLookupElement {
  @Suppress("JoinDeclarationAndAssignment")
  private val delegate: CommitLookupElement

  constructor(delegate: CommitLookupElement) : super(delegate.index, delegate.priority, delegate.provider) {
    this.delegate = delegate
    putUserData(DIRECT_INSERTION, true)
  }

  override fun handleInsert(context: InsertionContext) {
    if (requiresCommittedDocuments()) {
      PsiDocumentManager
        .getInstance(context.project)
        .commitDocument(context.document)
    }

    runWriteAction {
      context.editor.removeSelection()
      delegate.handleInsert(context)
    }
  }

  override fun getPsiElement(): CommitFakePsiElement =
    delegate.psiElement

  override fun getLookupString(): String =
    delegate.lookupString

  override fun getDisplayedText(): String =
    delegate.getDisplayedText()

  override fun renderElement(presentation: LookupElementPresentation) {
    delegate.renderElement(presentation)
  }

  override fun requiresCommittedDocuments(): Boolean =
    delegate.requiresCommittedDocuments()

  override fun isValid(): Boolean =
    delegate.isValid

  override fun isCaseSensitive(): Boolean =
    delegate.isCaseSensitive

  override fun getAutoCompletionPolicy(): AutoCompletionPolicy =
    delegate.autoCompletionPolicy

  override fun getAllLookupStrings(): Set<String> =
    delegate.allLookupStrings

  override fun isWorthShowingInAutoPopup(): Boolean =
    delegate.isWorthShowingInAutoPopup

  override fun getObject(): Any =
    delegate.getObject()

  override fun toString(): String =
    "$delegate"

  override fun equals(other: Any?): Boolean =
    if (other is ContextLookupElementDecorator) {
      delegate == other.delegate
    } else {
      false
    }

  override fun hashCode(): Int =
    delegate.hashCode()
}
