package com.github.lppedd.cc.parser

import kotlin.contracts.contract
import kotlin.internal.InlineOnly
import kotlin.math.max

/**
 * @author Edoardo Luppi
 */
internal object CCParser {
  private const val TYPE = "type"
  private const val SCOPE = "scope"
  private const val BRK_CHANGE = "brkChange"
  private const val SEPARATOR = "separator"
  private const val SUBJECT = "subject"
  private const val FOOTER_TYPE = "footerType"
  private const val FOOTER = "footer"

  private val headerRegExp = """
    |(?:.*? |)??(?<$TYPE>[a-zA-Z0-9-]+)
    |(?<$SCOPE>(?:\([^()\r\n]*\)|\(.*(?=!)|\(.*(?=:))|\(.*(?=$))?
    |(?<$BRK_CHANGE>!)?
    |(?<$SEPARATOR>:)? ?
    |(?<$SUBJECT>(?<=:).+)?$
  """
    .trimMargin()
    .replace("\n", "")
    .trim()
    .toRegex()

  private val footerRegExp = """
    ^(?<$FOOTER_TYPE>[ a-zA-Z0-9-]+)?
    (?<$SEPARATOR>:)?
    (?<$FOOTER>(?<=:)(?:.|\s(?!\s))*)?
  """
    .trimIndent()
    .replace("\n", "")
    .trim()
    .toRegex()

  fun parseHeader(segment: CharSequence): CommitTokens {
    val groups = headerRegExp.matchEntire(segment)?.groups ?: return CommitTokens()
    return CommitTokens(
      type = groups[TYPE]?.run { ValidToken(value, range.forCaretModel()) } ?: InvalidToken,
      scope = buildCommitScope(groups[SCOPE]),
      breakingChange = BreakingChange(groups[BRK_CHANGE] != null),
      separator = Separator(groups[SEPARATOR] != null),
      subject = groups[SUBJECT]?.run { ValidToken(value, range.forCaretModel()) } ?: InvalidToken
    )
  }

  fun parseFooter(text: CharSequence): FooterTokens {
    val groups = footerRegExp.find(text)?.groups ?: return FooterTokens()
    return FooterTokens(
      type = groups[FOOTER_TYPE]?.run { ValidToken(value, range.forCaretModel()) } ?: InvalidToken,
      separator = Separator(groups[SEPARATOR] != null),
      footer = groups[FOOTER]?.run { ValidToken(value, range.forCaretModel()) } ?: InvalidToken
    )
  }

  private fun buildCommitScope(matchGroup: MatchGroup?): Scope {
    if (matchGroup == null) {
      return InvalidToken
    }

    var value = StringBuilder(matchGroup.value) as CharSequence
    var startIndex = matchGroup.range.first
    var endIndex = matchGroup.range.last

    if (value.first() == '(') {
      value = value.drop(1)
      startIndex++
      endIndex++
    }

    if (value.isNotEmpty() && value.last() == ')') {
      value = value.dropLast(1)
      endIndex--
    }

    return ValidToken("$value".trim(), IntRange(startIndex, max(startIndex, endIndex)))
  }
}

internal interface Token
internal interface Type : Token
internal interface Scope : Token
internal interface Subject : Token
internal interface FooterType : Token
internal interface Footer : Token
internal inline class BreakingChange(val isPresent: Boolean)
internal inline class Separator(val isPresent: Boolean)

internal object InvalidToken :
    Type,
    Scope,
    Subject,
    FooterType,
    Footer

internal class ValidToken(val value: String, val range: IntRange) :
    Type,
    Scope,
    Subject,
    FooterType,
    Footer

internal fun Token.isInContext(offset: Int): Boolean {
  contract { returns(true) implies (this@isInContext is ValidToken) }
  return this is ValidToken && range.contains(offset)
}

@InlineOnly
private inline fun IntRange.forCaretModel(): IntRange =
  IntRange(first, maxOf(1, last + 1))
