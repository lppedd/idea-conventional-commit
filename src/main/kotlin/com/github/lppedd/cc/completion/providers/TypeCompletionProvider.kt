package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.api.CommitType
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.github.lppedd.cc.vcs.RecentCommitsService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TypeCompletionProvider(
    private val project: Project,
    private val context: TypeCommitContext,
) : CompletionProvider<CommitTypeProvider> {
  override fun getProviders(): Collection<CommitTypeProvider> =
    project.service<CommitTokenProviderService>().getTypeProviders()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val prefixedResultSet = resultSet.withPrefixMatcher(context.type)
    val recentCommitsService = project.service<RecentCommitsService>()
    val recentTypes = recentCommitsService.getRecentTypes()
    val types = LinkedHashSet<ProviderCommitToken<CommitType>>(64)

    // Despite all the sorting logic being in CommitLookupElementWeigher, we need sorted
    // providers here too as per user configuration to produce de-duplicated tokens correctly.
    // For example, if providers A and B produce the same token C, and
    // if B is ordered before A, then only B's token will be shown in completion.
    // Browsing the Platform code it seems the Lookup implementation already does that,
    // but it is better to do it ourselves beforehand
    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        provider.getCommitTypes(context.type)
          .asSequence()
          .take(CC.Provider.MaxItems)
          .forEach { types.add(ProviderCommitToken(provider, it)) }
      }
    }

    types.forEachIndexed { index, (provider, commitType) ->
      val psiElement = CommitTypePsiElement(project, commitType.getText())
      val element = CommitTypeLookupElement(psiElement, commitType)
      element.putUserData(ELEMENT_INDEX, index)
      element.putUserData(ELEMENT_PROVIDER, provider)
      element.putUserData(ELEMENT_IS_RECENT, recentTypes.contains(commitType.getValue()))
      prefixedResultSet.addElement(element)
    }
  }
}
