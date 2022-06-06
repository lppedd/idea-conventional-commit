package com.github.lppedd.cc.language.lexer

import com.intellij.lexer.FlexLexer

/**
 * @author Edoardo Luppi
 */
interface EofCapableFlexLexer : FlexLexer {
  fun isEof(): Boolean

  fun setEof(isEof: Boolean)
}
