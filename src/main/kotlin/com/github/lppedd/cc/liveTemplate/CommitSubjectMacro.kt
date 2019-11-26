package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitSubjectMacro : CommitMacro() {
  override fun getName() = "commitSubject"
  override fun getPresentableName() = "commitSubject()"
  override fun queryProviders(project: Project, lookup: LookupImpl) {
    val psiManager = PsiManager.getInstance(project)
    CommitSubjectProvider.EP_NAME.getExtensions(project)
      .flatMap { it.getCommitSubjects("", "") }
      .map { CommitSubjectPsiElement(it, psiManager) }
      .mapIndexed(::CommitSubjectLookupElement)
      .forEach { lookup.addItem(it, PrefixMatcher.ALWAYS_TRUE) }
  }
}
