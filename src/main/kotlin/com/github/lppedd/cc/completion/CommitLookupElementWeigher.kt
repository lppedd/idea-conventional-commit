package com.github.lppedd.cc.completion

import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.completion.providers.ELEMENT_INDEX
import com.github.lppedd.cc.completion.providers.ELEMENT_IS_RECENT
import com.github.lppedd.cc.completion.providers.ELEMENT_PROVIDER
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal class CommitLookupElementWeigher(
    private val configService: CCConfigService,
) : LookupElementWeigher("commitLookupElementWeigher") {
  private companion object {
    private const val scopePriority = 1
    private const val subjectPriority = 1
    private const val footerValuePriority = 1

    // Those three could appear in the same completion invocation
    private const val typePriority = 3
    private const val bodyPriority = 2
    private const val footerTypePriority = 1

    private val defaultWeight = CommitTokenWeight(0, false, 0, 0)
  }

  // Lower priority = lower weight = higher-up in the list
  override fun weigh(element: LookupElement): Comparable<*> =
    if (element is CommitTokenLookupElement) {
      getTokenWeight(element)
    } else {
      defaultWeight
    }

  private fun getTokenWeight(element: CommitTokenLookupElement): CommitTokenWeight {
    // The token type's priority.
    // This is used to prioritize a certain token's type when multiple types
    // might be shown in the same completion invocation
    // (e.g. a footer type token should be shown before a body token)
    val tokenPriority = getTokenPriority(element)

    // The token's provider priority.
    // Providers can be ordered via settings by the user, so each
    val provider = element.getUserData(ELEMENT_PROVIDER)
    val providerPriority = getProviderPriority(provider)

    // The token's index, in ascending order, in the context of extraction from its provider
    val index = element.getUserData(ELEMENT_INDEX)!!

    // Indicate if the token has been used recently by the user.
    // This flag is set by each CompletionProvider
    val isRecentToken = element.getUserData(ELEMENT_IS_RECENT) ?: false
    val isRecentlyUsed = configService.isPrioritizeRecentlyUsed && isRecentToken

    return CommitTokenWeight(tokenPriority, isRecentlyUsed, providerPriority, index)
  }

  private fun getTokenPriority(element: CommitTokenLookupElement): Int =
    when (element) {
      is ContextLookupElementDecorator -> getTokenPriority(element.getDelegate())
      is TemplateLookupElementDecorator -> getTokenPriority(element.getDelegate())
      is TextFieldLookupElementDecorator -> getTokenPriority(element.getDelegate())
      is CommitTypeLookupElement -> typePriority
      is CommitScopeLookupElement -> scopePriority
      is CommitNoScopeLookupElement -> scopePriority
      is CommitSubjectLookupElement -> subjectPriority
      is CommitBodyLookupElement -> bodyPriority
      is CommitFooterTypeLookupElement -> footerTypePriority
      is CommitFooterValueLookupElement -> footerValuePriority
      is ShowMoreCoAuthorsLookupElement -> footerValuePriority
      else -> Int.MAX_VALUE
    }

  private fun getProviderPriority(provider: CommitTokenProvider?): Int =
    when (provider) {
      is CommitTypeProvider -> configService.getProviderOrder(provider)
      is CommitScopeProvider -> configService.getProviderOrder(provider)
      is CommitSubjectProvider -> configService.getProviderOrder(provider)
      is CommitBodyProvider -> configService.getProviderOrder(provider)
      is CommitFooterTypeProvider -> configService.getProviderOrder(provider)
      is CommitFooterValueProvider -> configService.getProviderOrder(provider)
      else -> Int.MAX_VALUE
    }

  private class CommitTokenWeight(
      private val tokenPriority: Int,
      private val isRecentlyUsed: Boolean,
      private val providerPriority: Int,
      private val index: Int,
  ) : Comparable<CommitTokenWeight> {
    companion object {
      private val comparator =
        Comparator.comparingInt(CommitTokenWeight::tokenPriority)
          .thenComparing { w1, w2 -> -w1.isRecentlyUsed.compareTo(w2.isRecentlyUsed) }
          .thenComparingInt(CommitTokenWeight::providerPriority)
          .thenComparingInt(CommitTokenWeight::index)
    }

    override fun compareTo(other: CommitTokenWeight): Int =
      comparator.compare(this, other)
  }
}
