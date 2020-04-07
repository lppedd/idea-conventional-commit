@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitFooterProvider
import com.github.lppedd.cc.api.FOOTER_EP
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitFooterTypeLookupElement
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class FooterTypeCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
) : CommitCompletionProvider<CommitFooterProvider> {
  private val footerProviders =
    FOOTER_EP.getExtensions(project)
      .asSequence()
      .sortedBy(CCConfigService.getInstance(project)::getProviderOrder)

  override val providers = footerProviders.toList()
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    footerProviders
      .flatMap { runWithCheckCanceled { it.getCommitFooterTypes().asSequence() } }
      .map { CommitFooterTypePsiElement(project, it) }
      .mapIndexed(::CommitFooterTypeLookupElement)
      .distinctBy(CommitLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
