package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.SCOPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.parser.CommitContext.ScopeCommitContext
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class ScopeCompletionProvider(
    private val project: Project,
    private val context: ScopeCommitContext,
) : CompletionProvider<CommitScopeProvider> {
  override val providers: List<CommitScopeProvider> = SCOPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.scope.trim())
    val config = project.service<CCConfigService>()

    providers.asSequence()
      .sortedBy(config::getProviderOrder)
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = ScopeProviderWrapper(project, provider)
          provider.getCommitScopes(context.type)
            .asSequence()
            .take(CC.Provider.MaxItems)
            .map { wrapper to it }
        }
      }
      .mapIndexed { index, (provider, commitScope) ->
        CommitScopeLookupElement(
          index,
          provider,
          CommitScopePsiElement(project, commitScope),
        )
      }
      .distinctBy(CommitScopeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
