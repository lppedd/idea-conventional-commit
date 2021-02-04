package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.github.lppedd.cc.invokeLaterOnEdt
import com.github.lppedd.cc.moveCaretToOffset
import com.github.lppedd.cc.psiElement.CommitFakePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key

/**
 * Decorates every [CommitLookupElement] when template completion is used
 * to provide additional features when specific lookup elements are selected.
 *
 * @author Edoardo Luppi
 */
internal class TemplateLookupElementDecorator : CommitLookupElement {
  @Suppress("JoinDeclarationAndAssignment")
  private val delegate: CommitLookupElement

  constructor(delegate: CommitLookupElement) : super(delegate.index, delegate.priority, delegate.provider) {
    this.delegate = delegate
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val templateState = TemplateManagerImpl.getTemplateState(editor) ?: return

    when (delegate) {
      is CommitNoScopeLookupElement -> deleteScopeAndNext(editor, templateState)
      is CommitFooterTypeLookupElement -> appendSeparatorOnFooterType(editor, templateState)
      is CommitBodyLookupElement -> templateState.gotoEnd()
      is ShowMoreCoAuthorsLookupElement -> delegate.handleInsert(context)
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
    true

  override fun getAutoCompletionPolicy(): AutoCompletionPolicy =
    delegate.autoCompletionPolicy

  override fun getAllLookupStrings(): Set<String> =
    delegate.allLookupStrings

  override fun isWorthShowingInAutoPopup(): Boolean =
    delegate.isWorthShowingInAutoPopup

  override fun getObject(): Any =
    delegate.getObject()

  override fun <T : Any?> getUserData(key: Key<T>): T? =
    delegate.getUserData(key)

  override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
    delegate.putUserData(key, value)
  }

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
    val (start, end) = templateState.getSegmentRange(INDEX_SCOPE)
    editor.document.deleteString(start, end)
    templateState.nextTab()
  }

  private fun appendSeparatorOnFooterType(editor: Editor, templateState: TemplateState) {
    val offset = templateState.getSegmentRange(INDEX_BODY_OR_FOOTER_TYPE).endOffset
    editor.document.insertString(offset, ": ")

    invokeLaterOnEdt {
      editor.moveCaretToOffset(offset + 2)
    }
  }
}
