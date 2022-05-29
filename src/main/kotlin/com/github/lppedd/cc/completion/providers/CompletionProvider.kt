package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.intellij.openapi.util.Key

internal val ELEMENT_INDEX: Key<Int> = Key.create("com.github.lppedd.cc.lookupElement.index")
internal val ELEMENT_PROVIDER: Key<CommitTokenProvider> = Key.create("com.github.lppedd.cc.lookupElement.provider")
internal val ELEMENT_IS_RECENT: Key<Boolean> = Key.create("com.github.lppedd.cc.lookupElement.isRecent")

/**
 * @author Edoardo Luppi
 */
internal interface CompletionProvider<out T : CommitTokenProvider> {
  fun getProviders(): Collection<T>

  fun stopHere(): Boolean

  fun complete(resultSet: ResultSet)
}
