package com.github.lppedd.cc.language.lexer

import com.intellij.lexer.FlexAdapter

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitLexer : FlexAdapter(ConventionalCommitFlexLexer(null)) {
  override fun getFlex(): EofCapableFlexLexer =
    super.getFlex() as EofCapableFlexLexer

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    flex.setEof(false)
    super.start(buffer, startOffset, endOffset, initialState)
  }
}
