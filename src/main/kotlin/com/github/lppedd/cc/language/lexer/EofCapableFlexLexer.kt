package com.github.lppedd.cc.language.lexer

import com.intellij.lexer.FlexLexer

/**
 * @author Edoardo Luppi
 */
public interface EofCapableFlexLexer : FlexLexer {
  public fun isEof(): Boolean
  public fun setEof(isEof: Boolean)
}
