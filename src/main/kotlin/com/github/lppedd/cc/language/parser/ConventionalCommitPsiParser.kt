package com.github.lppedd.cc.language.parser

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
      builder.advanceLexer()
    }

    message.done(ConventionalCommitElementType.COMMIT_MESSAGE)
    file.done(root)

    return builder.treeBuilt
  }
}
