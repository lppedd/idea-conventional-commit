package com.github.lppedd.cc

import kotlin.math.max

data class PCommitToken(
  val value: String = "",
  val range: IntRange = IntRange(0, 0),
  val isValid: Boolean = true
) {
  fun isInContext(i: Int) = isValid && range.contains(i)
}

typealias PCommitType = PCommitToken
typealias PCommitScope = PCommitToken
typealias PCommitSubject = PCommitToken

private val EMPTY = PCommitToken(isValid = false)

data class PCommitTokens(
  val type: PCommitType = EMPTY,
  val scope: PCommitScope = EMPTY,
  val brkChange: Boolean = false,
  val separator: Boolean = false,
  val subject: PCommitSubject = EMPTY
)

/**
 * @author Edoardo Luppi
 */
object CCParser {
  private const val TYPE = "type"
  private const val SCOPE = "scope"
  private const val BRK_CHANGE = "brkChange"
  private const val SEPARATOR = "separator"
  private const val SUBJECT = "subject"

  private val regexp = """
    |(?:(.*? )|)??(?<$TYPE>[a-zA-Z0-9]+)
    |(?<$SCOPE>(?:\([^()\r\n]*\)|\(.*(?=!)|\(.*(?=:))|\(.*(?=$))?
    |(?<$BRK_CHANGE>!)?
    |(?<$SEPARATOR>:)? ?
    |(?<$SUBJECT>(?<=:).+)?$
  """
    .trimMargin("|")
    .replace("\n", "")
    .trim()
    .toRegex()

  fun parseText(text: String): PCommitTokens {
    val groups = regexp.matchEntire(text)?.groups ?: return PCommitTokens()
    return PCommitTokens(
      type = buildCommitToken(groups[TYPE]),
      scope = buildCommitScope(groups[SCOPE]),
      brkChange = groups[BRK_CHANGE] != null,
      separator = groups[SEPARATOR] != null,
      subject = buildCommitToken(groups[SUBJECT])
    )
  }

  private fun buildCommitToken(matchGroup: MatchGroup?): PCommitToken =
    if (matchGroup == null) PCommitToken(isValid = false)
    else PCommitToken(matchGroup.value, matchGroup.range)

  private fun buildCommitScope(matchGroup: MatchGroup?): PCommitScope {
    if (matchGroup == null) {
      return PCommitScope(isValid = false)
    }

    var value = matchGroup.value

    if (value[0] == '(') {
      value = value.drop(1)
    }

    if (value.isNotEmpty() && value[value.lastIndex] == ')') {
      value = value.dropLast(1)
    }

    val startIndex = matchGroup.range.first
    val endIndex = matchGroup.range.last
    return PCommitScope(value.trim(), IntRange(startIndex, max(startIndex, endIndex)))
  }
}
