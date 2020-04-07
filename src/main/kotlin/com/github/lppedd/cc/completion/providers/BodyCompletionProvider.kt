@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.BODY_EP
import com.github.lppedd.cc.api.CommitBodyProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitBodyLookupElement
import com.github.lppedd.cc.lookupElement.CommitLookupElement
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
  private val bodyProviders =
    BODY_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)

  override val providers = bodyProviders.toList()
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    bodyProviders
      .flatMap {
        runWithCheckCanceled {
          it.getCommitBodies(
            (commitTokens.type as? ValidToken)?.value,
            (commitTokens.scope as? ValidToken)?.value,
            (commitTokens.subject as? ValidToken)?.value
          ).asSequence()
        }
      }
      .map { CommitBodyPsiElement(project, it) }
      .mapIndexed { i, psi -> CommitBodyLookupElement(i, psi, context.type) }
      .distinctBy(CommitLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
