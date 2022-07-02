package com.github.lppedd.cc.language.parser

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitPsiParser : PsiParser {
  override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
    val fileMarker = builder.mark()
    val messageMarker = builder.mark()

    while (!builder.eof()) {
      when (builder.tokenType) {
        ConventionalCommitTokenType.PAREN_LEFT -> parseScope(builder)
        ConventionalCommitTokenType.FOOTER_TYPE -> parseFooter(builder)
        ConventionalCommitTokenType.FOOTER_TYPE_BREAKING_CHANGE -> parseFooter(builder)
        else -> builder.advanceLexer()
      }
    }

    messageMarker.done(ConventionalCommitElementType.COMMIT_MESSAGE)
    fileMarker.done(root)

    return builder.treeBuilt
  }

  private fun parseScope(builder: PsiBuilder) {
    val marker = builder.mark()
    var token = builder.advanceAndGet()

    if (token == ConventionalCommitTokenType.SCOPE) {
      token = builder.advanceAndGet()
    }

    if (token == ConventionalCommitTokenType.PAREN_RIGHT) {
      builder.advanceLexer()
    }

    marker.done(ConventionalCommitElementType.SCOPE)
  }

  private fun parseFooter(builder: PsiBuilder) {
    val marker = builder.mark()
    var token = builder.advanceAndGet()

    if (token == ConventionalCommitTokenType.FOOTER_SEPARATOR) {
      token = builder.advanceAndGet()
    }

    if (token == ConventionalCommitTokenType.FOOTER_VALUE) {
      builder.advanceLexer()
    }

    marker.done(ConventionalCommitElementType.FOOTER)
  }

  private fun PsiBuilder.advanceAndGet(): IElementType? {
    advanceLexer()
    return tokenType
  }
}
