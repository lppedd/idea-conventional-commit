@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.api.CommitFooterTypeProvider
import com.github.lppedd.cc.api.FOOTER_TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitFooterTypeLookupElement
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class FooterTypeCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
) : CompletionProvider<CommitFooterTypeProvider> {
  override val providers: List<CommitFooterTypeProvider> = FOOTER_TYPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet, shouldCheckCanceled: Boolean) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.asSequence()
      .flatMap { provider ->
        runWithCheckCanceled(shouldCheckCanceled) {
          val wrapper = FooterTypeProviderWrapper(project, provider)
          provider.getCommitFooterTypes()
            .asSequence()
            .take(MAX_ITEMS_PER_PROVIDER)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitFooterTypePsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitFooterTypeLookupElement(i, provider, psi) }
      .distinctBy(CommitFooterTypeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
