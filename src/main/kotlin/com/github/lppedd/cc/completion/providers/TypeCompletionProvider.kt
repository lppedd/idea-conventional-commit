package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.api.CommitType
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.github.lppedd.cc.vcs.RecentCommitsService
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TypeCompletionProvider(
  private val project: Project,
  private val context: TypeCommitContext,
) : CompletionProvider<CommitTypeProvider> {
  override fun getProviders(): Collection<CommitTypeProvider> =
    CommitTokenProviderService.getInstance(project).getTypeProviders()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val prefixedResultSet = resultSet.withPrefixMatcher(context.type)
    val recentCommitsService = RecentCommitsService.getInstance(project)
    val recentTypes = recentCommitsService.getRecentTypes()
    val types = LinkedHashSet<ProviderCommitToken<CommitType>>(64)

    // Despite all the sorting logic being in CommitLookupElementWeigher, we need sorted
    // providers here too as per user configuration to produce deduplicated tokens correctly.
    // For example, if providers A and B produce the same token C, and
    // if B is ordered before A, then only B's token will be shown in completion.
    // Browsing the Platform code, it seems the Lookup implementation already does that,
    // but it is better to do it ourselves beforehand
    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        provider.getCommitTypes(context.type)
          .asSequence()
          .take(CompletionProvider.MaxItems)
          .forEach { types.add(ProviderCommitToken(provider, it)) }
      }
    }

    types.forEachIndexed { index, (provider, commitType) ->
      val psiElement = CommitTypePsiElement(project, commitType.getText())
      val element = CommitTypeLookupElement(psiElement, commitType)
      element.putUserData(LookupElementKey.Index, index)
      element.putUserData(LookupElementKey.Provider, provider)
      element.putUserData(LookupElementKey.IsRecent, recentTypes.contains(commitType.getValue()))
      prefixedResultSet.addElement(element)
    }
  }
}
