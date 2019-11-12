package com.github.lppedd.cc

import com.github.lppedd.cc.extensions.DefaultConventionalCommitProvider
import kotlin.math.max

data class CommitToken(
  val value: String = "",
  val range: IntRange = IntRange(0, 0),
  val isValid: Boolean = true
) {
  fun isInContext(i: Int) = isValid && range.contains(i)
}

typealias CommitType = CommitToken
typealias CommitScope = CommitToken
typealias CommitSubject = CommitToken

private val EMPTY = CommitToken(isValid = false)

data class CommitTokens(
  val type: CommitType = EMPTY,
  val scope: CommitScope = EMPTY,
  val brkChange: Boolean = false,
  val separator: Boolean = false,
  val subject: CommitSubject = EMPTY
)

/**
 * @author Edoardo Luppi
 */
object ConventionalCommitParser {
  private const val TYPE = "type"
  private const val SCOPE = "scope"
  private const val BRK_CHANGE = "brkChange"
  private const val SEPARATOR = "separator"
  private const val SUBJECT = "subject"

  private val regexp = """
    .*(?<$TYPE>${DefaultConventionalCommitProvider.TYPES.joinToString("|") { it.name }})
    (?<$SCOPE>(?:\([^()\r\n]*\)|\())?
    (?<$BRK_CHANGE>!)?
    (?<$SEPARATOR>:)? ?
    (?<$SUBJECT>.+$)?.*
  """
    .trimIndent()
    .replace("\n", "")
    .trim()
    .toRegex()

  fun parseText(text: String): CommitTokens {
    val groups = regexp.matchEntire(text)?.groups ?: return CommitTokens()
    return CommitTokens(
      type = buildCommitToken(groups[TYPE]),
      scope = buildCommitScope(groups[SCOPE]),
      brkChange = groups[BRK_CHANGE] != null,
      separator = groups[SEPARATOR] != null,
      subject = buildCommitToken(groups[SUBJECT])
    )
  }

  private fun buildCommitToken(matchGroup: MatchGroup?): CommitToken =
    if (matchGroup == null) CommitToken(isValid = false)
    else CommitToken(matchGroup.value, matchGroup.range)

  private fun buildCommitScope(matchGroup: MatchGroup?): CommitScope {
    if (matchGroup == null) {
      return CommitScope(isValid = false)
    }

    var value = matchGroup.value
    var startIndex = matchGroup.range.first
    val endIndex = matchGroup.range.last

    if (value[0] == '(') {
      value = value.drop(1)
      ++startIndex
    }

    if (value.isNotEmpty() && value[value.lastIndex] == ')') {
      value = value.dropLast(1)
    }

    return CommitScope(value, IntRange(startIndex, max(startIndex, endIndex)))
  }
}
