package com.github.lppedd.cc.completion

import com.github.lppedd.cc.flattenWhitespaces
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.completion.PrefixMatcher

/**
 * A prefix matcher for completion items which strips whitespaces
 * characters `[\r\n\t\f\v ]` from lookup string.
 * This means even lookup elements which spawn multiple lines
 * can be searched by the user.
 *
 * @author Edoardo Luppi
 */
internal class FlatPrefixMatcher(prefix: String) : PlainPrefixMatcher(prefix) {
  override fun isStartMatch(name: String): Boolean =
    name.flattenWhitespaces().startsWith(prefix, true)

  override fun prefixMatches(name: String): Boolean =
    name.flattenWhitespaces().contains(prefix, true)

  override fun cloneWithPrefix(prefix: String): PrefixMatcher =
    FlatPrefixMatcher(prefix)
}
