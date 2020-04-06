package com.github.lppedd.cc.parser

import com.github.lppedd.cc.parser.CommitContext.*

/**
 * @author Edoardo Luppi
 */
data class CommitTokens(
    val type: Type = InvalidToken,
    val scope: Scope = InvalidToken,
    val breakingChange: BreakingChange = BreakingChange(false),
    val separator: Separator = Separator(false),
    val subject: Subject = InvalidToken,
) {
  fun getContext(offset: Int): CommitContext? {
    return when {
      offset == 0 -> TypeCommitContext("")
      separator.isPresent -> SubjectCommitContext(
        (type as ValidToken).value,
        (scope as? ValidToken)?.value ?: "",
        (subject as? ValidToken)?.value ?: "",
      )
      scope.isInContext(offset) -> ScopeCommitContext((type as ValidToken).value, scope.value)
      type.isInContext(offset) -> TypeCommitContext(type.value)
      else -> null
    }
  }
}

sealed class CommitContext {
  data class TypeCommitContext(val type: String) : CommitContext()
  data class ScopeCommitContext(val type: String, val scope: String) : CommitContext()
  data class SubjectCommitContext(val type: String, val scope: String, val subject: String) : CommitContext()
}
