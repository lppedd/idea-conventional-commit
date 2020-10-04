@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.api.CommitFooterTypeProvider
import com.github.lppedd.cc.api.FOOTER_TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitFooterTypeLookupElement
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
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
    providers.asSequence()
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = FooterTypeProviderWrapper(project, provider)
          provider.getCommitFooterTypes()
            .asSequence()
            .take(MAX_ITEMS_PER_PROVIDER)
            .map { wrapper to it }
        }
      }
      .mapIndexed { index, (provider, commitFooterType) ->
        CommitFooterTypeLookupElement(
          index,
          provider,
          CommitFooterTypePsiElement(project, commitFooterType),
        )
      }
      .distinctBy(CommitFooterTypeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
