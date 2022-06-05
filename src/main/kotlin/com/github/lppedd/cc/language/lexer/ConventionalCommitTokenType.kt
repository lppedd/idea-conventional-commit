package com.github.lppedd.cc.language.lexer

import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitTokenType(debugName: String) : IElementType(debugName, ConventionalCommitLanguage) {
  companion object {
    @JvmField val TYPE: IElementType = ConventionalCommitTokenType("TYPE")
    @JvmField val SCOPE: IElementType = ConventionalCommitTokenType("SCOPE")
    @JvmField val PAREN_LEFT: IElementType = ConventionalCommitTokenType("PAREN_LEFT")
    @JvmField val PAREN_RIGHT: IElementType = ConventionalCommitTokenType("PAREN_RIGHT")
    @JvmField val BREAKING_CHANGE: IElementType = ConventionalCommitTokenType("BREAKING_CHANGE")
    @JvmField val SEPARATOR: IElementType = ConventionalCommitTokenType("SEPARATOR")
    @JvmField val SUBJECT: IElementType = ConventionalCommitTokenType("SUBJECT")
    @JvmField val BODY: IElementType = ConventionalCommitTokenType("BODY")
    @JvmField val FOOTER_TYPE: IElementType = ConventionalCommitTokenType("FOOTER_TYPE")
    @JvmField val FOOTER_TYPE_BREAKING_CHANGE: IElementType = ConventionalCommitTokenType("FOOTER_TYPE_BREAKING_CHANGE")
    @JvmField val FOOTER_VALUE: IElementType = ConventionalCommitTokenType("FOOTER_VALUE")
  }
}
