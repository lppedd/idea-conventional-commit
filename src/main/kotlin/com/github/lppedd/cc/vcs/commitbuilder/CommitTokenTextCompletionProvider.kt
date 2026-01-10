package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.completion.ConventionalCommitLookupElementWeigher
import com.github.lppedd.cc.completion.FlatPrefixMatcher
import com.github.lppedd.cc.configuration.CCConfigService
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.CharFilter.Result
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.util.textCompletion.TextCompletionProvider

/**
 * Called by [com.intellij.util.textCompletion.TextCompletionContributor].
 *
 * @author Edoardo Luppi
 * @see CommitBuilderDialog
 */
internal abstract class CommitTokenTextCompletionProvider(private val project: Project) :
    TextCompletionProvider,
    DumbAware {
  abstract fun fillVariants(prefix: String, resultSet: CompletionResultSet)

  override fun getAdvertisement(): String? =
    null

  override fun getPrefix(text: String, offset: Int): String =
    text.take(offset)

  override fun applyPrefixMatcher(resultSet: CompletionResultSet, prefix: String): CompletionResultSet =
    resultSet
      .caseInsensitive()
      .withPrefixMatcher(FlatPrefixMatcher(prefix))
      .withRelevanceSorter(
        CompletionSorter.emptySorter()
          .weigh(PreferStartMatching())
          .weigh(ConventionalCommitLookupElementWeigher(CCConfigService.getInstance(project)))
      )

  override fun acceptChar(ch: Char): Result? =
    null

  override fun fillCompletionVariants(
    parameters: CompletionParameters,
    prefix: String,
    resultSet: CompletionResultSet,
  ) {
    fillVariants(prefix, resultSet)
    resultSet.stopHere()
  }
}
