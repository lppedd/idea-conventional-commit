package com.github.lppedd.cc.parser

import com.intellij.openapi.util.TextRange

/**
 * @author Edoardo Luppi
 */
internal class CCTextRange(startOffset: Int, endOffset: Int) : TextRange(startOffset, endOffset) {
  override fun contains(offset: Int): Boolean =
    offset in startOffset..endOffset
}
