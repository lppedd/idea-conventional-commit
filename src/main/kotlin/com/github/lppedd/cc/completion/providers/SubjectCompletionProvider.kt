package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.parser.CommitContext.SubjectCommitContext
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.components.service
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
    val config = project.service<CCConfigService>()

    providers.asSequence()
      .sortedBy(config::getProviderOrder)
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = SubjectProviderWrapper(project, provider)
          provider.getCommitSubjects(context.type, context.scope)
            .asSequence()
            .take(CC.Provider.MaxItems)
            .map { wrapper to it }
        }
      }
      .mapIndexed { index, (provider, commitSubject) ->
        CommitSubjectLookupElement(
            index,
            provider,
            CommitSubjectPsiElement(project, commitSubject),
        )
      }
      .distinctBy(CommitSubjectLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
