package com.github.lppedd.cc.completion.menu

import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement
import org.jetbrains.annotations.ApiStatus

/**
 * A prefix matcher which simply filters out lookup elements.
 *
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class FilterPrefixMatcher(
    private val delegate: PrefixMatcher,
    prefix: String? = null,
) : PrefixMatcher(prefix ?: delegate.prefix) {
  override fun prefixMatches(element: LookupElement): Boolean =
    false

  override fun prefixMatches(name: String): Boolean =
    false

  override fun isStartMatch(element: LookupElement?): Boolean =
    false

  override fun isStartMatch(name: String?): Boolean =
    false

  override fun cloneWithPrefix(prefix: String): PrefixMatcher =
    FilterPrefixMatcher(delegate, prefix)
}
