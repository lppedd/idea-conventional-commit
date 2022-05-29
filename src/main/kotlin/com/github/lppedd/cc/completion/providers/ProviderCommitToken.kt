package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.api.CommitTokenProvider

/**
 * @author Edoardo Luppi
 */
internal data class ProviderCommitToken<out T : CommitToken>(
    val provider: CommitTokenProvider,
    val token: T,
) : CommitToken by token {
  override fun toString(): String =
    "${provider.getPresentation().getName()}: $token"

  override fun equals(other: Any?): Boolean =
    getValue() == (other as? ProviderCommitToken<*>)?.getValue()

  override fun hashCode(): Int =
    getValue().hashCode()
}
