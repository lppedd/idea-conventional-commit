@file:Suppress("deprecation")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitFooterValueProvider
import com.github.lppedd.cc.api.FOOTER_VALUE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitFooterValueLookupElement
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.lookupElement.ShowMoreCoAuthorsLookupElement
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterValueContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.codeInsight.completion.CompletionProcess
import com.intellij.codeInsight.completion.CompletionProgressIndicator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class FooterValueCompletionProvider(
    private val project: Project,
    private val context: FooterValueContext,
    private val commitTokens: CommitTokens,
    private val process: CompletionProcess,
) : CompletionProvider<CommitFooterValueProvider> {
  override val providers: List<CommitFooterValueProvider> = FOOTER_VALUE_EP.getExtensions(project)
  override val stopHere = true

  override fun complete(resultSet: ResultSet) {
    val prefix = context.value.trimStart()
    val rs = resultSet.withPrefixMatcher(prefix)
    val config = project.service<CCConfigService>()

    providers.asSequence()
      .sortedBy(config::getProviderOrder)
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = FooterValueProviderWrapper(project, provider)
          provider.getCommitFooterValues(
              context.type,
              (commitTokens.type as? ValidToken)?.value,
              (commitTokens.scope as? ValidToken)?.value,
              (commitTokens.subject as? ValidToken)?.value,
          )
            .asSequence()
            .take(CC.Provider.MaxItems)
            .map { wrapper to it }
        }
      }
      .mapIndexed { index, (provider, commitFooterValue) ->
        CommitFooterValueLookupElement(
            index,
            provider,
            CommitFooterValuePsiElement(project, commitFooterValue),
        )
      }
      .distinctBy(CommitFooterValueLookupElement::getLookupString)
      .forEach(rs::addElement)

    if ("co-authored-by".equals(context.type, true)) {
      rs.addElement(buildShowMoreLookupElement(prefix))
    }

    rs.stopHere()
  }

  private fun buildShowMoreLookupElement(prefix: String): CommitLookupElement {
    val lookupElement = ShowMoreCoAuthorsLookupElement(project, prefix)

    @Suppress("UnstableApiUsage")
    if (process is CompletionProgressIndicator) {
      process.lookup.addPrefixChangeListener(lookupElement, process.lookup)
    }

    return lookupElement
  }
}
