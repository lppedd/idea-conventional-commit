package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitBody
import com.github.lppedd.cc.api.CommitBodyProvider
import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitBodyLookupElement
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class BodyCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
    private val commitTokens: CommitTokens,
) : CompletionProvider<CommitBodyProvider> {
  override fun getProviders(): Collection<CommitBodyProvider> =
    project.service<CommitTokenProviderService>().getBodyProviders()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val prefixedResultSet = resultSet.withPrefixMatcher(context.type)
    val bodies = LinkedHashSet<ProviderCommitToken<CommitBody>>(64)

    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        val commitBodies = provider.getCommitBodies(
            (commitTokens.type as? ValidToken)?.value ?: "",
            (commitTokens.scope as? ValidToken)?.value ?: "",
            (commitTokens.subject as? ValidToken)?.value ?: "",
        )

        commitBodies.asSequence()
          .take(CC.Provider.MaxItems)
          .forEach { bodies.add(ProviderCommitToken(provider, it)) }
      }
    }

    bodies.forEachIndexed { index, (provider, commitBody) ->
      val psiElement = CommitBodyPsiElement(project, commitBody.getText())
      val element = CommitBodyLookupElement(psiElement, commitBody)
      element.putUserData(ELEMENT_INDEX, index)
      element.putUserData(ELEMENT_PROVIDER, provider)
      prefixedResultSet.addElement(element)
    }
  }
}
