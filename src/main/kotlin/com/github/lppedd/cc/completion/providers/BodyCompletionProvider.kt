@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.BODY_EP
import com.github.lppedd.cc.api.CommitBodyProvider
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitBodyLookupElement
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class BodyCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
    private val commitTokens: CommitTokens,
) : CommitCompletionProvider<CommitBodyProvider> {
  override val providers: List<CommitBodyProvider> = BODY_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.asSequence()
      .flatMap { provider ->
        runWithCheckCanceled {
          val wrapper = BodyProviderWrapper(provider)
          provider.getCommitBodies(
              (commitTokens.type as? ValidToken)?.value,
              (commitTokens.scope as? ValidToken)?.value,
              (commitTokens.subject as? ValidToken)?.value
            )
            .asSequence()
            .take(200)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitBodyPsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitBodyLookupElement(i, provider, psi, context.type) }
      .distinctBy(CommitBodyLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}

internal class BodyProviderWrapper(private val provider: CommitBodyProvider) : ProviderWrapper {
  override fun getId(): String =
    provider.getId()

  override fun getPresentation(): ProviderPresentation =
    provider.getPresentation()

  override fun getPriority(project: Project) =
    Priority(CCConfigService.getInstance(project).getProviderOrder(provider))
}
