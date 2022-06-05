package com.github.lppedd.cc.language.lexer

import com.intellij.lexer.FlexAdapter

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitLexer : FlexAdapter(ConventionalCommitFlexLexer(null))
