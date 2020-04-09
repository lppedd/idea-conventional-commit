@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.api.TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class TypeCompletionProvider(
    private val project: Project,
    private val context: TypeCommitContext,
) : CompletionProvider<CommitTypeProvider> {
  override val providers: List<CommitTypeProvider> = TYPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.asSequence()
      .flatMap { provider ->
        runWithCheckCanceled {
          val wrapper = TypeProviderWrapper(project, provider)
          provider.getCommitTypes(context.type)
            .asSequence()
            .take(MAX_ITEMS_PER_PROVIDER)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitTypePsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitTypeLookupElement(i, provider, psi) }
      .distinctBy(CommitTypeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
