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
    val file = builder.mark()
    val message = builder.mark()

    while (!builder.eof()) {
      val tokenType = builder.tokenType

      if (tokenType == ConventionalCommitTokenType.PAREN_LEFT) {
        parseScope(builder)
      }

      builder.advanceLexer()
    }

    message.done(ConventionalCommitElementType.COMMIT_MESSAGE)
    file.done(root)

    return builder.treeBuilt
  }

  private fun parseScope(builder: PsiBuilder) {
    val mark = builder.mark()
    var token = builder.advanceAndGet()

    if (token == ConventionalCommitTokenType.SCOPE) {
      token = builder.advanceAndGet()
    }

    if (token == ConventionalCommitTokenType.PAREN_RIGHT) {
      builder.advanceLexer()
    }

    mark.done(ConventionalCommitElementType.SCOPE)
  }

  private fun PsiBuilder.advanceAndGet(): IElementType? {
    advanceLexer()
    return tokenType
  }
}
