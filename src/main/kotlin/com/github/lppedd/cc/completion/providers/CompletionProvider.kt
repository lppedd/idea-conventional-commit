package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.resultset.ResultSet

/**
 * @author Edoardo Luppi
 */
internal interface CompletionProvider<out T : CommitTokenProvider> {
  @Suppress("ConstPropertyName")
  companion object {
    const val MaxItems: Int = 200
  }

  fun getProviders(): Collection<T>
  fun stopHere(): Boolean
  fun complete(resultSet: ResultSet)
}
