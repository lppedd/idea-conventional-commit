package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.completion.CommitLookupElementWeigher
import com.github.lppedd.cc.completion.FlatPrefixMatcher
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.CharFilter.Result
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.openapi.project.DumbAware
import com.intellij.util.textCompletion.TextCompletionProvider

/**
 * Called by [com.intellij.util.textCompletion.TextCompletionContributor].
 *
 * @author Edoardo Luppi
 * @see CommitBuilderDialog
 */
internal abstract class CommitTokenCompletionProvider : TextCompletionProvider, DumbAware {
  abstract fun fillVariants(prefix: String, resultSet: CompletionResultSet)

  override fun getAdvertisement(): String? =
    null

  override fun getPrefix(text: String, offset: Int): String =
    text.take(offset)

  override fun applyPrefixMatcher(resultSet: CompletionResultSet, prefix: String): CompletionResultSet =
    resultSet
      .caseInsensitive()
      .withPrefixMatcher(FlatPrefixMatcher(prefix))
      .withRelevanceSorter(sorter(CommitLookupElementWeigher))

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

  private fun sorter(weigher: LookupElementWeigher): CompletionSorter =
    (CompletionSorter.emptySorter() as CompletionSorterImpl)
      .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
      .withClassifier(CompletionSorterImpl.weighingFactory(weigher))
}
