package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.BODY_EP
import com.github.lppedd.cc.api.CommitBody
import com.github.lppedd.cc.api.CommitBodyProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.executeOnPooledThread
import com.github.lppedd.cc.lookupElement.CommitBodyLookupElement
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class BodyCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
    private val commitTokens: CommitTokens,
) : CompletionProvider<CommitBodyProvider> {
  override val providers: List<CommitBodyProvider> = BODY_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.map {
        safeRunWithCheckCanceled {
          val provider = BodyProviderWrapper(project, it)
          val futureData = executeOnPooledThread {
            it.getCommitBodies(
              (commitTokens.type as? ValidToken)?.value,
              (commitTokens.scope as? ValidToken)?.value,
              (commitTokens.subject as? ValidToken)?.value
            )
          }

          provider with futureData
        }
      }
      .asSequence()
      .map { (provider, futureData) -> retrieveItems(provider, futureData) }
      .sortedBy { (provider) -> provider.getPriority() }
      .flatMap { (provider, types) -> buildLookupElements(provider, types, context.type) }
      .distinctBy(LookupElement::getLookupString)
      .forEach(rs::addElement)
  }

  private fun buildLookupElements(
      provider: BodyProviderWrapper,
      bodies: Collection<CommitBody>,
      prefix: String,
  ): Sequence<CommitBodyLookupElement> =
    bodies.asSequence().mapIndexed { index, type ->
      val psi = CommitBodyPsiElement(project, type)
      CommitBodyLookupElement(index, provider, psi, prefix)
    }
}
