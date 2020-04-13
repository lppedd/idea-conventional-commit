package com.github.lppedd.cc.completion.providers

import kotlin.internal.InlineOnly

/**
 * @author Edoardo Luppi
 */
internal data class ResultCarrier<out P : ProviderWrapper, out T>(
    val provider: P,
    val data: T,
)

@InlineOnly
internal inline infix fun <P : ProviderWrapper, D> P.with(that: D): ResultCarrier<P, D> =
  ResultCarrier(this, that)
