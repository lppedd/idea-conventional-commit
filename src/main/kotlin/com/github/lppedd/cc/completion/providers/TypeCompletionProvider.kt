package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitType
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.api.TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.executeOnPooledThread
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TypeCompletionProvider(
    private val project: Project,
    private val context: TypeCommitContext,
) : CompletionProvider<CommitTypeProvider> {
  override val providers: List<CommitTypeProvider> = TYPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.map {
        safeRunWithCheckCanceled {
          val provider = TypeProviderWrapper(project, it)
          val futureData = executeOnPooledThread { it.getCommitTypes(context.type) }
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
      provider: TypeProviderWrapper,
      types: Collection<CommitType>,
  ): Sequence<CommitTypeLookupElement> =
    types.asSequence().mapIndexed { index, type ->
      val psi = CommitTypePsiElement(project, type)
      CommitTypeLookupElement(index, provider, psi)
    }
}
