package com.github.lppedd.cc.parser

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

  private val regExp = """
    |(?:(.*? )|)??(?<$TYPE>[a-zA-Z0-9-]+)
    |(?<$SCOPE>(?:\([^()\r\n]*\)|\(.*(?=!)|\(.*(?=:))|\(.*(?=$))?
    |(?<$BRK_CHANGE>!)?
    |(?<$SEPARATOR>:)? ?
    |(?<$SUBJECT>(?<=:).+)?$
  """
    .trimMargin()
    .replace("\n", "")
    .trim()
    .toRegex()

  fun parseText(text: CharSequence): CommitTokens {
    val groups = regExp.matchEntire(text)?.groups ?: return CommitTokens()
    return CommitTokens(
      type = groups[TYPE]?.run { ValidToken(value, range.normalize()) } ?: InvalidToken,
      scope = buildCommitScope(groups[SCOPE]),
      breakingChange = BreakingChange(groups[BRK_CHANGE] != null),
      separator = Separator(groups[SEPARATOR] != null),
      subject = groups[SUBJECT]?.run { ValidToken(value, range.normalize()) } ?: InvalidToken
    )
  }

  private fun buildCommitScope(matchGroup: MatchGroup?): Scope {
    if (matchGroup == null) {
      return InvalidToken
    }

    var value = StringBuilder(matchGroup.value) as CharSequence

    if (value.first() == '(') {
      value = value.drop(1)
    }

    if (value.isNotEmpty() && value.last() == ')') {
      value = value.dropLast(1)
    }

    val startIndex = matchGroup.range.first
    val endIndex = matchGroup.range.last
    return ValidToken("$value".trim(),
      IntRange(startIndex, max(startIndex, endIndex)))
  }
}

internal interface Token
internal interface Type : Token
internal interface Scope : Token
internal interface Subject : Token
internal inline class BreakingChange(val isPresent: Boolean)
internal inline class Separator(val isPresent: Boolean)

internal class ValidToken(
    val value: String,
    val range: IntRange,
) : Type, Scope, Subject

internal object InvalidToken : Type, Scope, Subject

@InlineOnly
private inline fun IntRange.normalize(): IntRange =
  IntRange(first, maxOf(1, last))
