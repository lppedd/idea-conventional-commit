package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitFooterType
import com.github.lppedd.cc.api.CommitFooterTypeProvider
import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitFooterTypeLookupElement
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class FooterTypeCompletionProvider(
  private val project: Project,
  private val context: FooterTypeContext,
) : CompletionProvider<CommitFooterTypeProvider> {
  override fun getProviders(): Collection<CommitFooterTypeProvider> =
    project.service<CommitTokenProviderService>().getFooterTypeProviders()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val prefixedResultSet = resultSet.withPrefixMatcher(context.type)
    val footerTypes = LinkedHashSet<ProviderCommitToken<CommitFooterType>>(64)

    // See comment in TypeCompletionProvider
    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        provider.getCommitFooterTypes()
          .asSequence()
          .take(CompletionProvider.MaxItems)
          .forEach { footerTypes.add(ProviderCommitToken(provider, it)) }
      }
    }

    footerTypes.forEachIndexed { index, (provider, commitFooterType) ->
      val psiElement = CommitFooterTypePsiElement(project, commitFooterType.getText())
      val element = CommitFooterTypeLookupElement(psiElement, commitFooterType)
      element.putUserData(LookupElementKey.Index, index)
      element.putUserData(LookupElementKey.Provider, provider)
      prefixedResultSet.addElement(element)
    }
  }
}
