package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitScope
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.parser.CommitContext.ScopeCommitContext
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.github.lppedd.cc.vcs.RecentCommitsService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class ScopeCompletionProvider(
    private val project: Project,
    private val context: ScopeCommitContext,
) : CompletionProvider<CommitScopeProvider> {
  override fun getProviders(): Collection<CommitScopeProvider> =
    project.service<CommitTokenProviderService>().getScopeProviders()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val prefixedResultSet = resultSet.withPrefixMatcher(context.scope.trim())
    val recentCommitsService = project.service<RecentCommitsService>()
    val recentScopes = recentCommitsService.getRecentScopes()
    val scopes = LinkedHashSet<ProviderCommitToken<CommitScope>>(64)

    // See comment in TypeCompletionProvider
    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        provider.getCommitScopes(context.type)
          .asSequence()
          .take(CC.Provider.MaxItems)
          .forEach { scopes.add(ProviderCommitToken(provider, it)) }
      }
    }

    scopes.forEachIndexed { index, (provider, commitScope) ->
      val psiElement = CommitScopePsiElement(project, commitScope.getText())
      val element = CommitScopeLookupElement(psiElement, commitScope)
      element.putUserData(ELEMENT_INDEX, index)
      element.putUserData(ELEMENT_PROVIDER, provider)
      element.putUserData(ELEMENT_IS_RECENT, recentScopes.contains(commitScope.getValue()))
      prefixedResultSet.addElement(element)
    }
  }
}
