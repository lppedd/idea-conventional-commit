@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.parser.CommitContext.SubjectCommitContext
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class SubjectCompletionProvider(
    private val project: Project,
    private val context: SubjectCommitContext,
) : CommitCompletionProvider<CommitSubjectProvider> {
  private val subjectProviders =
    SUBJECT_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)

  override val providers = subjectProviders.toList()
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.subject.trimStart())
    subjectProviders.flatMap {
        runWithCheckCanceled {
          it.getCommitSubjects(context.type, context.scope).asSequence()
        }
      }
      .map { CommitSubjectPsiElement(project, it) }
      .mapIndexed(::CommitSubjectLookupElement)
      .distinctBy(CommitSubjectLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
