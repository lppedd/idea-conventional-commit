@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.parser.CommitContext.SubjectCommitContext
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class SubjectCompletionProvider(
    private val project: Project,
    private val context: SubjectCommitContext,
) : CompletionProvider<CommitSubjectProvider> {
  override val providers: List<CommitSubjectProvider> = SUBJECT_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.subject.trimStart())
    providers.asSequence()
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = SubjectProviderWrapper(project, provider)
          provider.getCommitSubjects(context.type, context.scope)
            .asSequence()
            .take(MAX_ITEMS_PER_PROVIDER)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitSubjectPsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitSubjectLookupElement(i, provider, psi) }
      .distinctBy(CommitSubjectLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
