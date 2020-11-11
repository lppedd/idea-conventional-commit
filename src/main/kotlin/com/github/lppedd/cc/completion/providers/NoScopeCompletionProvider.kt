package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitNoScopeLookupElement
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class NoScopeCompletionProvider(project: Project) : CompletionProvider<CommitScopeProvider> {
  private val commitNoScopeLookupElement = CommitNoScopeLookupElement(project)

  override val providers = emptyList<CommitScopeProvider>()
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    resultSet
      .withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
      .addElement(commitNoScopeLookupElement)
  }
}
