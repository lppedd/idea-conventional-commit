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

@JvmOverloads
@JvmName("parse")
@Suppress("FoldInitializerAndIfToElvis")
internal fun parseConventionalCommit(message: String, lenient: Boolean = false): ParseResult {
  val reader = createReader(message)
  val type = reader.consume(CCToken.Type.TYPE)

  if (type == null) {
    return ParseResult.Error("The commit type is missing or invalid")
  }

  if (!lenient && type.any(Char::isWhitespace)) {
    return ParseResult.Error("The commit type '$type' is invalid")
  }

  var scope: String? = null

  if (reader.consumeIf(CCToken.Type.SCOPE_OPEN_PAREN)) {
    scope = reader.consume(CCToken.Type.SCOPE)

    // This message is valid in non-lenient mode:
    //  'build(  ): updated dev dependencies'
    if (scope == null) {
      if (!lenient) {
        return ParseResult.Error("The commit scope is missing or invalid")
      }

      // This message is valid in lenient mode:
      //  'build(): updated dev dependencies'
      scope = ""
    }

    if (!reader.consumeIf(CCToken.Type.SCOPE_CLOSE_PAREN)) {
      return ParseResult.Error("The commit scope is missing the closing parenthesis")
    }
  }

  val isBreakingChange = reader.consume(CCToken.Type.BREAKING_CHANGE) != null

  // The ':' subject separator is required to recognize a Conventional Commits message,
  // even in lenient mode
  if (!reader.consumeIf(CCToken.Type.SEPARATOR)) {
    return ParseResult.Error("The ':' separator is missing after the type/scope")
  }

  var subject = reader.consume(CCToken.Type.SUBJECT)

  if (subject.isNullOrBlank()) {
    if (!lenient) {
      return ParseResult.Error("The commit subject is missing or invalid")
    }

    // In lenient mode these messages are valid:
    //  'build:'
    //  'build:   '
    subject = subject ?: ""
  }

  val body = reader.consume(CCToken.Type.BODY)
  val footers = ArrayList<CommitFooter>()

  while (reader.current?.type == CCToken.Type.FOOTER_TYPE) {
    val footerType = reader.takeAndAdvance()

    // Consume/skip the separator (':' or ' '), if present
    reader.consume(CCToken.Type.SEPARATOR)

    val footerValue = reader.consume(CCToken.Type.FOOTER_VALUE)

    if (!lenient) {
      // If the footer type does not have an associated value,
      // we can disregard trailing whitespace when validating it
      val fv = if (footerValue.isNullOrBlank()) footerType.trimEnd() else footerType

      // The BREAKING CHANGE footer type is a special case
      if (!fv.equals("BREAKING CHANGE", ignoreCase = true) && fv.any(Char::isWhitespace)) {
        return ParseResult.Error("The commit footer type '$footerType' is invalid")
      }
    }

    // Allow a missing footer value even in non-lenient mode
    footers += CommitFooter(footerType, footerValue ?: "")
  }

  return ParseResult.Success(
    CommitMessage(
      type = type,
      scope = scope,
      isBreakingChange = isBreakingChange,
      subject = subject,
      body = body,
      footers = footers,
    )
  )
}

private fun createReader(message: String): CCTokenReader {
  val lexer = SpecConventionalCommitFlexLexer(null)
  lexer.reset(message, 0, message.length, SpecConventionalCommitFlexLexer.YYINITIAL)
  return CCTokenReader(lexer)
}

private class CCTokenReader(private val lexer: SpecConventionalCommitFlexLexer) {
  var current: CCToken? = lexer.advance()
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
