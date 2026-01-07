package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.invokeLaterOnEdt
import com.github.lppedd.cc.moveCaretToOffset
import com.github.lppedd.cc.psiElement.CommitTokenPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase

/**
 * Decorates every [CommitTokenLookupElement] when template completion is used
 * to provide additional features when specific lookup elements are selected.
 *
 * @author Edoardo Luppi
 */
internal class TemplateLookupElementDecorator(private val delegate: CommitTokenLookupElement) :
    CommitTokenLookupElement(),
    DelegatingLookupElement<CommitTokenLookupElement> {
  override fun getDelegate(): CommitTokenLookupElement =
    delegate

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val templateState = TemplateManagerImpl.getTemplateState(editor) ?: return

    when (delegate) {
      is CommitNoScopeLookupElement -> deleteScopeAndNext(editor, templateState)
      is CommitFooterTypeLookupElement -> appendSeparatorOnFooterType(editor, templateState)
      is CommitBodyLookupElement -> templateState.gotoEnd()
      is ShowMoreCoAuthorsLookupElement -> delegate.handleInsert(context)
      else -> {}
    }
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
    true

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
    if (other is TemplateLookupElementDecorator) {
      delegate == other.delegate
    } else {
      false
    }

  override fun hashCode(): Int =
    delegate.hashCode()

  private fun deleteScopeAndNext(editor: Editor, templateState: TemplateState) {
    val (start, end) = templateState.getSegmentRange(TemplateSegment.Scope)
    editor.document.deleteString(start, end)
    templateState.nextTab()
  }

  private fun appendSeparatorOnFooterType(editor: Editor, templateState: TemplateState) {
    val offset = templateState.getSegmentRange(TemplateSegment.BodyOrFooterType).endOffset
    editor.document.insertString(offset, ": ")

    invokeLaterOnEdt {
      editor.moveCaretToOffset(offset + 2)
    }
  }
}
