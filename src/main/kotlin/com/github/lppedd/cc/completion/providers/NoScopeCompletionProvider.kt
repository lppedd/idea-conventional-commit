package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitNoScopeLookupElement
import com.github.lppedd.cc.psiElement.NoScopeCommitPsiElement
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class NoScopeCompletionProvider(
  private val project: Project,
) : CompletionProvider<CommitScopeProvider> {
  override fun getProviders(): Collection<CommitScopeProvider> =
    emptyList()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val element = CommitNoScopeLookupElement(NoScopeCommitPsiElement(project))
    element.putUserData(LookupElementKey.Index, Int.MAX_VALUE)
    resultSet.withPrefixMatcher(PlainPrefixMatcher.ALWAYS_TRUE).addElement(element)
  }
}
