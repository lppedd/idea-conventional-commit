@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.SCOPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.parser.CommitContext.ScopeCommitContext
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class ScopeCompletionProvider(
    private val project: Project,
    private val context: ScopeCommitContext,
) : CommitCompletionProvider<CommitScopeProvider> {
  private val scopeProviders =
    SCOPE_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)

  override val providers = scopeProviders.toList()
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.scope.trim())
    scopeProviders
      .flatMap { runWithCheckCanceled { it.getCommitScopes(context.type).asSequence() } }
      .map { CommitScopePsiElement(project, it) }
      .mapIndexed(::CommitScopeLookupElement)
      .distinctBy(CommitScopeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
