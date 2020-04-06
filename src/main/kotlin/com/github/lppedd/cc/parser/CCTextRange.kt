package com.github.lppedd.cc.parser

import com.intellij.openapi.util.TextRange
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class CCTextRange(startOffset: Int, endOffset: Int) : TextRange(startOffset, endOffset) {
  @Suppress("ConvertTwoComparisonsToRangeCheck")
  override fun contains(offset: Int): Boolean =
    startOffset <= offset && offset <= endOffset
}
