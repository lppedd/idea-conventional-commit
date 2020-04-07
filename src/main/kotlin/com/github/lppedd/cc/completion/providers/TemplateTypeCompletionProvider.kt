@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.api.TYPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.TemplateCommitTypeLookupElement
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class TemplateTypeCompletionProvider(
    private val project: Project,
    private val context: TypeCommitContext,
) : CommitCompletionProvider<CommitTypeProvider> {
  private val typeProviders =
    TYPE_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)

  override val providers = typeProviders.toList()
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    typeProviders
      .flatMap { runWithCheckCanceled { it.getCommitTypes("") }.asSequence() }
      .map { commitType -> CommitTypePsiElement(project, commitType) }
      .mapIndexed(::TemplateCommitTypeLookupElement)
      .distinctBy(TemplateCommitTypeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}