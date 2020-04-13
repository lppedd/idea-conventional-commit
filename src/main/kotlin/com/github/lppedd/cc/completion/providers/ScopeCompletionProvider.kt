package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitScope
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.SCOPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.executeOnPooledThread
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.parser.CommitContext.ScopeCommitContext
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.codeInsight.lookup.LookupElement
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
    providers.map {
        safeRunWithCheckCanceled {
          val provider = ScopeProviderWrapper(project, it)
          val futureData = executeOnPooledThread { it.getCommitScopes(context.type) }
          provider with futureData
        }
      }
      .asSequence()
      .map { (provider, futureData) -> retrieveItems(provider, futureData) }
      .sortedBy { (provider) -> provider.getPriority() }
      .flatMap { (provider, types) -> buildLookupElements(provider, types) }
      .distinctBy(LookupElement::getLookupString)
      .forEach(rs::addElement)
  }

  private fun buildLookupElements(
      provider: ScopeProviderWrapper,
      types: Collection<CommitScope>,
  ): Sequence<CommitScopeLookupElement> =
    types.asSequence().mapIndexed { index, type ->
      val psi = CommitScopePsiElement(project, type)
      CommitScopeLookupElement(index, provider, psi)
    }
}
