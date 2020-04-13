package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.MAX_ITEMS_PER_PROVIDER
import com.github.lppedd.cc.api.CommitTokenElement
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException

/**
 * @author Edoardo Luppi
 */
internal interface CompletionProvider<out T : CommitTokenProvider> {
  val providers: Collection<T>
  val stopHere: Boolean

  fun complete(resultSet: ResultSet)
}

internal fun <P : ProviderWrapper, T : CommitTokenElement> retrieveItems(
    provider: P,
    futureData: Future<Collection<T>>,
): ResultCarrier<P, Collection<T>> {
  try {
    val timeout = provider.timeout().toMillis().coerceAtMost(2500)
    val items = futureData.get(timeout, MILLISECONDS).take(MAX_ITEMS_PER_PROVIDER)
    return provider with items
  } catch (ignored: TimeoutException) {
    futureData.cancel(true)
  } catch (ignored: Exception) {
    // We're not interested in any recovery logic for now
  }

  return provider with emptyList()
}
