package com.github.lppedd.cc.parser

import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.FooterContext.FooterValueContext

/**
 * @author Edoardo Luppi
 */
internal data class FooterTokens(
    val type: FooterType = InvalidToken,
    val separator: Separator = Separator(false),
    val footer: Footer = InvalidToken,
) {
  fun getContext(offset: Int): FooterContext? {
    return when {
      offset == 0 -> FooterTypeContext("")
      separator.isPresent -> FooterValueContext(
        (type as ValidToken).value,
        (footer as? ValidToken)?.value ?: "",
      )
      type.isInContext(offset) -> FooterTypeContext(type.value)
      else -> null
    }
  }
}

internal sealed class FooterContext {
  data class FooterTypeContext(val type: String) : FooterContext()
  data class FooterValueContext(val type: String, val value: String) : FooterContext()
}
