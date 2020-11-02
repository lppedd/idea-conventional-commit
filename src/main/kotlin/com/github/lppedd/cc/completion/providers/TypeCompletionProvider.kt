package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.api.TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TypeCompletionProvider(
    private val project: Project,
    private val context: TypeCommitContext,
) : CompletionProvider<CommitTypeProvider> {
  override val providers: List<CommitTypeProvider> = TYPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.asSequence()
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = TypeProviderWrapper(project, provider)
          provider.getCommitTypes(context.type)
            .asSequence()
            .take(CC.Provider.MaxItems)
            .map { wrapper to it }
        }
      }
      .mapIndexed { index, (provider, commitType) ->
        CommitTypeLookupElement(
          index,
          provider,
          CommitTypePsiElement(project, commitType),
        )
      }
      .distinctBy(CommitTypeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
