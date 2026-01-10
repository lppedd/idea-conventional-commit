package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.psiElement.CommitTokenPsiElement
import com.github.lppedd.cc.removeSelection
import com.github.lppedd.cc.runInWriteActionIfNeeded
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase.DIRECT_INSERTION
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiDocumentManager

/**
 * Decorates every [CommitTokenLookupElement] when standard code completion is used
 * to provide a stripped-down implementation of the full lookup element
 * insertion cycle (when [DIRECT_INSERTION] is not set).
 *
 * @author Edoardo Luppi
 */
internal class ContextLookupElementDecorator(private val delegate: CommitTokenLookupElement) :
    CommitTokenLookupElement(),
    DelegatingLookupElement<CommitTokenLookupElement> {
  init {
    putUserData(DIRECT_INSERTION, true)
  }

  override fun getDelegate(): CommitTokenLookupElement =
    delegate

  override fun handleInsert(context: InsertionContext) = runInWriteActionIfNeeded {
    if (requiresCommittedDocuments()) {
      PsiDocumentManager.getInstance(context.project).commitDocument(context.document)
    }

    context.editor.removeSelection()
    delegate.handleInsert(context)
  }

  override fun getToken(): CommitToken =
    delegate.getToken()

  override fun getPsiElement(): CommitTokenPsiElement =
    delegate.psiElement

  override fun getLookupString(): String =
    delegate.lookupString

  override fun getItemText(): String =
    delegate.getItemText()

  override fun renderElement(presentation: LookupElementPresentation) =
    delegate.renderElement(presentation)

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

  override fun <T : Any> getUserData(key: Key<T>): T? =
    delegate.getUserData(key)

  override fun <T : Any> putUserData(key: Key<T>, value: T?) =
    delegate.putUserData(key, value)

  override fun <T : Any> putUserDataIfAbsent(key: Key<T>, value: T): T =
    delegate.putUserDataIfAbsent(key, value)

  override fun <T : Any> replace(key: Key<T>, oldValue: T?, newValue: T?): Boolean =
    delegate.replace(key, oldValue, newValue)

  override fun copyUserDataTo(other: UserDataHolderBase) =
    delegate.copyUserDataTo(other)

  override fun <T : Any> getCopyableUserData(key: Key<T>): T? =
    delegate.getCopyableUserData(key)

  override fun <T : Any> putCopyableUserData(key: Key<T>, value: T) =
    delegate.putCopyableUserData(key, value)

  override fun copyCopyableDataTo(clone: UserDataHolderBase) =
    delegate.copyCopyableDataTo(clone)

  override fun isUserDataEmpty(): Boolean =
    delegate.isUserDataEmpty

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
