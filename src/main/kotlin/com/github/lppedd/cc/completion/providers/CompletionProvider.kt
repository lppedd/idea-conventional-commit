@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.resultset.ResultSet

/**
 * @author Edoardo Luppi
 */
internal interface CompletionProvider<out T : CommitTokenProvider> {
  val providers: Collection<T>
  val stopHere: Boolean

  fun complete(resultSet: ResultSet)
}
