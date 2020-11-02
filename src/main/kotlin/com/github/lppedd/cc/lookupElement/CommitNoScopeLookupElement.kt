package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCIcons
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
internal class CommitNoScopeLookupElement(project: Project) :
    CommitLookupElement(
      -1,
      PRIORITY_SCOPE,
      object : ProviderWrapper {
        override fun getPriority() = Priority(0)
        override fun getId() = ""
        override fun getPresentation() = ProviderPresentation("Default", CCIcons.Logo)
      },
    ) {
  private val psiElement = object : CommitFakePsiElement(project) {}

  override fun getPsiElement(): CommitFakePsiElement =
    psiElement

  override fun getLookupString(): String =
    ""

  override fun renderElement(presentation: LookupElementPresentation) =
    presentation.let {
      it.itemText = "No scope"
      it.isItemTextItalic = true
    }
}
