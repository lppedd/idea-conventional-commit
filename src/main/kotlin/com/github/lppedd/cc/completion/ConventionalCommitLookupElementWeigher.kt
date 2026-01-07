package com.github.lppedd.cc.completion

import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitLookupElementWeigher(
  private val configService: CCConfigService,
) : LookupElementWeigher("commitLookupElementWeigher") {
  private companion object {
    private const val PRIORITY_SCOPE = 1
    private const val PRIORITY_SUBJECT = 1
    private const val PRIORITY_FOOTER_VALUE = 1

    // Those three could appear in the same completion invocation
    private const val PRIORITY_TYPE = 3
    private const val PRIORITY_BODY = 2
    private const val PRIORITY_FOOTER_TYPE = 1

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
    // The token type priority.
    // This is used to prioritize a certain token type when multiple types
    // might be shown in the same completion invocation (e.g., a footer type
    // token should be shown before a body token).
    val tokenPriority = getTokenPriority(element)

    // The token provider priority.
    // Providers can be ordered via settings by the user.
    val provider = element.getUserData(LookupElementKey.Provider)
    val providerPriority = getProviderPriority(provider)

    // The token index, in ascending order, in the context of extraction from its provider
    val index = element.getUserData(LookupElementKey.Index) ?: error("missing element index")

    // Indicate if the user has used the token recently.
    // This flag is set by each CompletionProvider.
    val isRecentToken = element.getUserData(LookupElementKey.IsRecent) ?: false
    val isRecentlyUsed = configService.prioritizeRecentlyUsed && isRecentToken

    return CommitTokenWeight(tokenPriority, isRecentlyUsed, providerPriority, index)
  }

  private fun getTokenPriority(element: CommitTokenLookupElement): Int =
    when (element) {
      is ContextLookupElementDecorator -> getTokenPriority(element.getDelegate())
      is TemplateLookupElementDecorator -> getTokenPriority(element.getDelegate())
      is TextFieldLookupElementDecorator -> getTokenPriority(element.getDelegate())
      is CommitTypeLookupElement -> PRIORITY_TYPE
      is CommitScopeLookupElement -> PRIORITY_SCOPE
      is CommitNoScopeLookupElement -> PRIORITY_SCOPE
      is CommitSubjectLookupElement -> PRIORITY_SUBJECT
      is CommitBodyLookupElement -> PRIORITY_BODY
      is CommitFooterTypeLookupElement -> PRIORITY_FOOTER_TYPE
      is CommitFooterValueLookupElement -> PRIORITY_FOOTER_VALUE
      is ShowMoreCoAuthorsLookupElement -> PRIORITY_FOOTER_VALUE
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
