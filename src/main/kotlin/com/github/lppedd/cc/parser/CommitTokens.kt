package com.github.lppedd.cc.parser

import com.github.lppedd.cc.parser.Context.*
import kotlin.contracts.contract

/**
 * @author Edoardo Luppi
 */
internal data class CommitTokens(
    val type: Type = InvalidToken,
    val scope: Scope = InvalidToken,
    val breakingChange: BreakingChange = BreakingChange(false),
    val separator: Separator = Separator(false),
    val subject: Subject = InvalidToken,
) {
  fun getContext(offset: Int): Context? {
    return when {
      offset == 0 -> TypeContext("")
      separator.isPresent -> SubjectContext(
        (type as ValidToken).value,
        (scope as? ValidToken)?.value ?: "",
        (subject as? ValidToken)?.value ?: "",
      )
      scope.isInContext(offset) -> ScopeContext((type as ValidToken).value, scope.value)
      type.isInContext(offset) -> TypeContext(type.value)
      else -> null
    }
  }
}

internal sealed class Context {
  data class TypeContext(val type: String) : Context()
  data class ScopeContext(val type: String, val scope: String) : Context()
  data class SubjectContext(val type: String, val scope: String, val subject: String) : Context()
}

private fun Token.isInContext(offset: Int): Boolean {
  contract {
    returns(true) implies (this@isInContext is ValidToken)
  }

  return this is ValidToken && range.contains(maxOf(0, offset - 1))
}
