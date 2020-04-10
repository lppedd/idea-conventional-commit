package com.github.lppedd.cc.completion.resultset

import com.github.lppedd.cc.invokeLaterOnEdt
import com.github.lppedd.cc.lookupElement.*
import com.github.lppedd.cc.moveCaretToOffset
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.util.ClassConditionKey
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class TemplateElementDecorator(private val delegate: CommitLookupElement) : CommitLookupElement() {
  override val index = delegate.index
  override val priority = delegate.priority
  override val provider = delegate.provider

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val templateState = TemplateManagerImpl.getTemplateState(editor) ?: return
    val document = editor.document

    when (delegate) {
      is CommitNoScopeLookupElement -> templateState.nextTab()
      is CommitFooterTypeLookupElement -> {
        val offset = templateState.getSegmentRange(INDEX_BODY_OR_FOOTER_TYPE).endOffset
        document.insertString(offset, ": ")

        invokeLaterOnEdt {
          editor.moveCaretToOffset(offset + 2)
        }
      }
      is CommitBodyLookupElement -> templateState.gotoEnd()
      is ShowMoreCoAuthorsLookupElement -> delegate.handleInsert(context)
    }
  }

  override fun getPsiElement() =
    delegate.psiElement

  override fun getLookupString() =
    delegate.lookupString

  override fun isValid() =
    delegate.isValid

  override fun isCaseSensitive() =
    delegate.isCaseSensitive

  override fun getAutoCompletionPolicy() =
    delegate.autoCompletionPolicy

  override fun getObject() =
    delegate.`object`

  override fun getUserDataString(): String? =
    delegate.userDataString

  override fun <T : Any?> `as`(conditionKey: ClassConditionKey<T>?) =
    delegate.`as`(conditionKey)

  override fun <T : Any?> `as`(clazz: Class<T>?) =
    delegate.`as`(clazz)

  override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
    delegate.putUserData(key, value)
  }

  override fun renderElement(presentation: LookupElementPresentation?) {
    delegate.renderElement(presentation)
  }

  override fun isWorthShowingInAutoPopup() =
    delegate.isWorthShowingInAutoPopup

  override fun <T : Any?> replace(key: Key<T>, oldValue: T?, newValue: T?) =
    delegate.replace(key, oldValue, newValue)

  override fun copyCopyableDataTo(clone: UserDataHolderBase) {
    delegate.copyCopyableDataTo(clone)
  }

  override fun copyUserDataTo(other: UserDataHolderBase) {
    delegate.copyUserDataTo(other)
  }

  override fun toString() =
    "$delegate"

  override fun isUserDataEmpty() =
    delegate.isUserDataEmpty

  override fun getAllLookupStrings(): MutableSet<String> =
    delegate.allLookupStrings

  override fun <T : Any?> putUserDataIfAbsent(key: Key<T>, value: T) =
    delegate.putUserDataIfAbsent(key, value)

  override fun <T : Any?> getUserData(key: Key<T>) =
    delegate.getUserData(key)

  override fun requiresCommittedDocuments() =
    delegate.requiresCommittedDocuments()

  override fun <T : Any?> getCopyableUserData(key: Key<T>): T =
    delegate.getCopyableUserData(key)

  override fun <T : Any?> putCopyableUserData(key: Key<T>, value: T) {
    delegate.putCopyableUserData(key, value)
  }

  override fun equals(other: Any?) =
    delegate == other

  override fun hashCode() =
    delegate.hashCode()
}
