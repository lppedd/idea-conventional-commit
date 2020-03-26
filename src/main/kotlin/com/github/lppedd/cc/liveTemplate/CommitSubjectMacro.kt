package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CommitSubjectMacro : CommitMacro() {
  override fun getName() = "commitSubject"

  override fun queryProviders(project: Project, lookup: LookupImpl) {
    SUBJECT_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)
      .flatMap { it.getCommitSubjects("", "").asSequence() }
      .map { CommitSubjectPsiElement(project, it) }
      .mapIndexed(::CommitSubjectLookupElement)
      .distinctBy(CommitSubjectLookupElement::getLookupString)
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
