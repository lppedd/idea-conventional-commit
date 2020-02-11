package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectMacro : CommitMacro() {
  override fun getName() = "commitSubject"
  override fun getPresentableName() = "commitSubject()"
  override fun queryProviders(project: Project, lookup: LookupImpl) {
    val psiManager = PsiManager.getInstance(project)
    val config = CCConfigService.getInstance(project)

    CommitSubjectProvider.EP_NAME.getExtensions(project)
      .asSequence()
      .sortedBy(config::getProviderOrder)
      .flatMap { it.getCommitSubjects("", "").asSequence() }
      .map { CommitSubjectPsiElement(it, psiManager) }
      .mapIndexed(::CommitSubjectLookupElement)
      .distinctBy(CommitSubjectLookupElement::getLookupString)
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
