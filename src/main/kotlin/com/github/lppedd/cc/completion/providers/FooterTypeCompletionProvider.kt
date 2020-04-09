@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitFooterProvider
import com.github.lppedd.cc.api.FOOTER_EP
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
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
) : CommitCompletionProvider<CommitFooterProvider> {
  override val providers: List<CommitFooterProvider> = FOOTER_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.asSequence()
      .flatMap { provider ->
        runWithCheckCanceled {
          val wrapper = FooterTypeProviderWrapper(project, provider)
          provider.getCommitFooterTypes()
            .asSequence()
            .take(200)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitFooterTypePsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitFooterTypeLookupElement(i, provider, psi) }
      .distinctBy(CommitFooterTypeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}

internal class FooterTypeProviderWrapper(
    project: Project,
    private val provider: CommitFooterProvider,
) : ProviderWrapper {
  private val config = CCConfigService.getInstance(project)

  override fun getId(): String =
    provider.getId()

  override fun getPresentation(): ProviderPresentation =
    provider.getPresentation()

  override fun getPriority() =
    Priority(config.getProviderOrder(provider))
}
