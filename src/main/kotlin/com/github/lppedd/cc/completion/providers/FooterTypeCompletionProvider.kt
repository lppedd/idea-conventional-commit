package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitFooterType
import com.github.lppedd.cc.api.CommitFooterTypeProvider
import com.github.lppedd.cc.api.FOOTER_TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.executeOnPooledThread
import com.github.lppedd.cc.lookupElement.CommitFooterTypeLookupElement
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class FooterTypeCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
) : CompletionProvider<CommitFooterTypeProvider> {
  override val providers: List<CommitFooterTypeProvider> = FOOTER_TYPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.map {
        safeRunWithCheckCanceled {
          val provider = FooterTypeProviderWrapper(project, it)
          val futureData = executeOnPooledThread(it::getCommitFooterTypes)
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
      provider: FooterTypeProviderWrapper,
      footerTypes: Collection<CommitFooterType>,
  ): Sequence<CommitFooterTypeLookupElement> =
    footerTypes.asSequence().mapIndexed { index, type ->
      val psi = CommitFooterTypePsiElement(project, type)
      CommitFooterTypeLookupElement(index, provider, psi)
    }
}
