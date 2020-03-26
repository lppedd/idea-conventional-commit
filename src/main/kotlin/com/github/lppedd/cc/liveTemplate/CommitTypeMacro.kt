package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.TYPE_EP
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CommitTypeMacro : CommitMacro() {
  override fun getName() = "commitType"

  override fun queryProviders(project: Project, lookup: LookupImpl) {
    TYPE_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)
      .flatMap { it.getCommitTypes("").asSequence() }
      .map { CommitTypePsiElement(project, it) }
      .mapIndexed(::CommitTypeLookupElement)
      .distinctBy(CommitTypeLookupElement::getLookupString)
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
