package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitSubject
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.executeOnPooledThread
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.parser.CommitContext.SubjectCommitContext
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.codeInsight.lookup.LookupElement
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
    providers.map {
        safeRunWithCheckCanceled {
          val provider = SubjectProviderWrapper(project, it)
          val futureData = executeOnPooledThread { it.getCommitSubjects(context.type, context.scope) }
          provider with futureData
        }
      }
      .asSequence()
      .map { (provider, futureData) -> retrieveItems(provider, futureData) }
      .sortedBy { (provider) -> provider.getPriority() }
      .flatMap { (provider, types) -> buildLookupElements(provider, types) }
      .distinctBy(LookupElement::getLookupString)
      .forEach(rs::addElement)
  }

  private fun buildLookupElements(
      provider: SubjectProviderWrapper,
      subjects: Collection<CommitSubject>,
  ): Sequence<CommitSubjectLookupElement> =
    subjects.asSequence().mapIndexed { index, type ->
      val psi = CommitSubjectPsiElement(project, type)
      CommitSubjectLookupElement(index, provider, psi)
    }
}
