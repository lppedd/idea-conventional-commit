@file:JvmName("ConventionalCommitParser")

package com.github.lppedd.cc.parser

import com.intellij.openapi.util.TextRange

internal sealed interface ParseResult {
  data class Success(@JvmField val message: CommitMessage) : ParseResult
  data class Error(@JvmField val message: String) : ParseResult
}

internal data class CommitFooter(
  @JvmField val type: String,
  @JvmField val value: String,
)

internal data class CommitMessage(
  @JvmField val type: String,
  @JvmField val scope: String?,
  @JvmField val isBreakingChange: Boolean,
  @JvmField val subject: String,
  @JvmField val body: String?,
  @JvmField val footers: List<CommitFooter>,
)

internal class CCToken(
  @JvmField val type: Type,
  @JvmField val value: String,
  @JvmField val range: TextRange,
) {
  enum class Type {
    TYPE,
    SCOPE_OPEN_PAREN,
    SCOPE,
    SCOPE_CLOSE_PAREN,
    BREAKING_CHANGE,
    SEPARATOR,
    SUBJECT,
    BODY,
    FOOTER_TYPE,
    FOOTER_VALUE,
    ERROR,
  }
}

@JvmName("parse")
@Suppress("FoldInitializerAndIfToElvis")
internal fun parseConventionalCommit(message: String): ParseResult {
  val reader = createReader(message.trim())
  val type = reader.consume(CCToken.Type.TYPE)

  if (type == null) {
    return ParseResult.Error("The commit type is missing or invalid")
  }

  var scope: String? = null

  if (reader.consumeIf(CCToken.Type.SCOPE_OPEN_PAREN)) {
    scope = reader.consume(CCToken.Type.SCOPE) ?: ""

    // Keeping this for behavior reference, as we do allow a missing scope.
    // For example, this is valid: 'build(): updated dev dependencies'
    //
    // if (scope == null) {
    //   return ParseResult.Error("The commit scope is missing or invalid")
    // }

    if (!reader.consumeIf(CCToken.Type.SCOPE_CLOSE_PAREN)) {
      return ParseResult.Error("The commit scope is missing the closing parenthesis")
    }
  }

  val isBreakingChange = reader.consume(CCToken.Type.BREAKING_CHANGE) != null

  if (!reader.consumeIf(CCToken.Type.SEPARATOR)) {
    return ParseResult.Error("The separator ':' is missing after the type/scope")
  }

  val subject = reader.consume(CCToken.Type.SUBJECT)

  if (subject == null) {
    return ParseResult.Error("The commit subject is missing or invalid")
  }

  val body = reader.consume(CCToken.Type.BODY)
  val footers = ArrayList<CommitFooter>()

  while (reader.current?.type == CCToken.Type.FOOTER_TYPE) {
    val footerType = reader.takeAndAdvance()

    // Consume/skip the separator, if any
    reader.consume(CCToken.Type.SEPARATOR)

    val footerValue = reader.consume(CCToken.Type.FOOTER_VALUE) ?: ""

    // Keeping this for behavior reference, as we do allow a missing footer value.
    //
    // if (footerValue == null) {
    //   return ParseResult.Error("The footer type '$footerType' is missing a value")
    // }

    footers += CommitFooter(footerType.trim(), footerValue.trim())
  }

  return ParseResult.Success(
    CommitMessage(
      type = type.trim(),
      scope = scope?.trim(),
      isBreakingChange = isBreakingChange,
      subject = subject.trim(),
      body = body?.trim(),
      footers = footers,
    )
  )
}

private fun createReader(message: String): CCTokenReader {
  val lexer = StrictConventionalCommitFlexLexer(null)
  lexer.reset(message, 0, message.length, StrictConventionalCommitFlexLexer.YYINITIAL)

  val initialToken = lexer.advance()
  return CCTokenReader(lexer, initialToken)
}

private class CCTokenReader(private val lexer: StrictConventionalCommitFlexLexer, initialToken: CCToken?) {
  var current: CCToken? = initialToken
    private set

  fun consume(expected: CCToken.Type): String? =
    if (current?.type == expected) {
      takeAndAdvance()
    } else {
      null
    }

  fun consumeIf(expected: CCToken.Type): Boolean =
    consume(expected) != null

  fun takeAndAdvance(): String {
    val token = current ?: error("expected a previously lexed token")
    val text = token.value
    current = lexer.advance()
    return text
  }
}
