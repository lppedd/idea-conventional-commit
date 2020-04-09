@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal interface CompletionProvider<out T : CommitTokenProvider> {
  val providers: Collection<T>
  val stopHere: Boolean

  fun complete(resultSet: ResultSet, shouldCheckCanceled: Boolean = true)
}
