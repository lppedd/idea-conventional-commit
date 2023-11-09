package com.github.lppedd.cc.language.lexer

import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
public class ConventionalCommitTokenType(debugName: String) : IElementType(debugName, ConventionalCommitLanguage) {
  public companion object {
    @JvmField public val TYPE: IElementType = ConventionalCommitTokenType("TYPE")
    @JvmField public val SCOPE_OPEN_PAREN: IElementType = ConventionalCommitTokenType("SCOPE_OPEN_PAREN")
    @JvmField public val SCOPE: IElementType = ConventionalCommitTokenType("SCOPE")
    @JvmField public val SCOPE_CLOSE_PAREN: IElementType = ConventionalCommitTokenType("SCOPE_CLOSE_PAREN")
    @JvmField public val BREAKING_CHANGE: IElementType = ConventionalCommitTokenType("BREAKING_CHANGE")
    @JvmField public val SEPARATOR: IElementType = ConventionalCommitTokenType("SEPARATOR")
    @JvmField public val SUBJECT: IElementType = ConventionalCommitTokenType("SUBJECT")
    @JvmField public val BODY: IElementType = ConventionalCommitTokenType("BODY")
    @JvmField public val FOOTER_TYPE: IElementType = ConventionalCommitTokenType("FOOTER_TYPE")
    @JvmField public val FOOTER_TYPE_BREAKING_CHANGE: IElementType = ConventionalCommitTokenType("FOOTER_TYPE_BREAKING_CHANGE")
    @JvmField public val FOOTER_VALUE: IElementType = ConventionalCommitTokenType("FOOTER_VALUE")
  }
}
