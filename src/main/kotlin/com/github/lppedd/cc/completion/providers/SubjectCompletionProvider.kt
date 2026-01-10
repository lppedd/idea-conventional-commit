package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitSubject
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.parser.CommitContext.SubjectCommitContext
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.github.lppedd.cc.vcs.RecentCommitsService
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class SubjectCompletionProvider(
  private val project: Project,
  private val context: SubjectCommitContext,
) : CompletionProvider<CommitSubjectProvider> {
  override fun getProviders(): Collection<CommitSubjectProvider> =
    CommitTokenProviderService.getInstance(project).getSubjectProviders()

  override fun stopHere(): Boolean =
    false

  override fun complete(resultSet: ResultSet) {
    val prefixedResultSet = resultSet.withPrefixMatcher(context.subject.trimStart())
    val recentCommitsService = RecentCommitsService.getInstance(project)
    val recentSubjects = recentCommitsService.getRecentSubjects()
    val subjects = LinkedHashSet<ProviderCommitToken<CommitSubject>>(64)

    getProviders().forEach { provider ->
      safeRunWithCheckCanceled {
        provider.getCommitSubjects(context.type, context.scope)
          .asSequence()
          .take(CompletionProvider.MaxItems)
          .forEach { subjects.add(ProviderCommitToken(provider, it)) }
      }
    }

    subjects.forEachIndexed { index, (provider, commitSubject) ->
      val psiElement = CommitSubjectPsiElement(project, commitSubject.getText())
      val element = CommitSubjectLookupElement(psiElement, commitSubject)
      element.putUserData(LookupElementKey.Index, index)
      element.putUserData(LookupElementKey.Provider, provider)
      element.putUserData(LookupElementKey.IsRecent, recentSubjects.contains(commitSubject.getValue()))
      prefixedResultSet.addElement(element)
    }
  }
}
