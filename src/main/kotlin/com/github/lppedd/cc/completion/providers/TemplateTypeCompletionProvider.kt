package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.api.CommitType
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.TemplateCommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.github.lppedd.cc.vcs.RecentCommitsService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TemplateTypeCompletionProvider(
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
    val allTypes = LinkedHashSet<ProviderCommitToken<CommitType>>(64)

    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        provider.getCommitTypes("")
          .asSequence()
          .take(CompletionProvider.MaxItems)
          .forEach { allTypes.add(ProviderCommitToken(provider, it)) }
      }
    }

    allTypes.forEachIndexed { index, (provider, commitType) ->
      val psiElement = CommitTypePsiElement(project, commitType.getText())
      val element = TemplateCommitTypeLookupElement(psiElement, commitType)
      element.putUserData(LookupElementKey.Index, index)
      element.putUserData(LookupElementKey.Provider, provider)
      element.putUserData(LookupElementKey.IsRecent, recentTypes.contains(commitType.getValue()))
      prefixedResultSet.addElement(element)
    }
  }
}
