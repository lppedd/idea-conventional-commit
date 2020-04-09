@file:Suppress("DEPRECATION", "UnstableApiUsage")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.api.CommitFooter
import com.github.lppedd.cc.api.CommitFooterProvider
import com.github.lppedd.cc.api.DefaultCommitTokenProvider
import com.github.lppedd.cc.api.FOOTER_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitFooterLookupElement
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.lookupElement.ShowMoreCoAuthorsLookupElement
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterValueContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterPsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.codeInsight.completion.CompletionProcess
import com.intellij.codeInsight.completion.CompletionProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class FooterValueCompletionProvider(
    private val project: Project,
    private val context: FooterValueContext,
    private val commitTokens: CommitTokens,
    private val process: CompletionProcess,
) : CompletionProvider<CommitFooterProvider> {
  override val providers: List<CommitFooterProvider> = FOOTER_EP.getExtensions(project)
  override val stopHere = true

  override fun complete(resultSet: ResultSet) {
    val prefix = context.value.trimStart()
    val rs = resultSet.withPrefixMatcher(prefix)

    providers.asSequence()
      .flatMap { provider ->
        runWithCheckCanceled {
          val wrapper = FooterValueProviderWrapper(project, provider)
          provider.getCommitFooters(
              context.type,
              (commitTokens.type as? ValidToken)?.value,
              (commitTokens.scope as? ValidToken)?.value,
              (commitTokens.subject as? ValidToken)?.value
            )
            .asSequence()
            .take(MAX_ITEMS_PER_PROVIDER)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitFooterPsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitFooterLookupElement(i, provider, psi, prefix) }
      .distinctBy(CommitFooterLookupElement::getLookupString)
      .forEach(rs::addElement)

    if ("co-authored-by".equals(context.type, true)) {
      rs.addElement(buildShowMoreLookupElement(prefix))
    }

    rs.stopHere()
  }

  private fun buildShowMoreLookupElement(prefix: String): CommitLookupElement {
    val commitFooter = CommitFooter("", CCBundle["cc.config.coAuthors.description"])
    val psiElement = CommitFooterPsiElement(project, commitFooter)
    val provider = FOOTER_EP.findExtensionOrFail(DefaultCommitTokenProvider::class.java, project)
    val wrapper = FooterValueProviderWrapper(project, provider)
    val lookupElement = ShowMoreCoAuthorsLookupElement(2000, wrapper, psiElement, prefix)

    if (process is CompletionProgressIndicator) {
      process.lookup.addPrefixChangeListener(lookupElement, process)
    }

    return lookupElement
  }
}
