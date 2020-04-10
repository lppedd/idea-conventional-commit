package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.ICON_DEFAULT_PRESENTATION
import com.github.lppedd.cc.PRIORITY_SCOPE
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.completion.Priority
import com.github.lppedd.cc.completion.providers.ProviderWrapper
import com.github.lppedd.cc.psiElement.CommitFakePsiElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitNoScopeLookupElement(private val project: Project) : CommitLookupElement() {
  override val index: Int = -1
  override val priority: Priority = PRIORITY_SCOPE
  override val provider: ProviderWrapper = object : ProviderWrapper {
    override fun getPriority() = Priority(0)
    override fun getId() = ""
    override fun getPresentation() = ProviderPresentation("Default", ICON_DEFAULT_PRESENTATION)
  }

  private val psiElement = object : CommitFakePsiElement(project) {}
  override fun getPsiElement(): CommitFakePsiElement = psiElement
  override fun getLookupString(): String = ""
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.itemText = "No scope"
    presentation.isItemTextItalic = true
  }
}
