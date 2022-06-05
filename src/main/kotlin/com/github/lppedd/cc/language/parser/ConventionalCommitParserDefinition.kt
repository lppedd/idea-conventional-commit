package com.github.lppedd.cc.language.parser

import com.github.lppedd.cc.language.ConventionalCommitFileType
import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.github.lppedd.cc.language.lexer.ConventionalCommitLexer
import com.github.lppedd.cc.language.psi.ConventionalCommitMessagePsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitPsiFile
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PlainTextTokenTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitParserDefinition : ParserDefinition {
  private companion object {
    private val FILE = IFileElementType(ConventionalCommitFileType.name, ConventionalCommitLanguage)
  }

  override fun createLexer(project: Project): Lexer =
    ConventionalCommitLexer()

  override fun createParser(project: Project): PsiParser =
    ConventionalCommitPsiParser()

  override fun getFileNodeType(): IFileElementType =
    FILE

  override fun getCommentTokens(): TokenSet =
    TokenSet.WHITE_SPACE

  override fun getStringLiteralElements(): TokenSet =
    TokenSet.create(PlainTextTokenTypes.PLAIN_TEXT)

  override fun createElement(astNode: ASTNode): PsiElement =
    when (astNode.elementType) {
      ConventionalCommitElementType.COMMIT_MESSAGE -> ConventionalCommitMessagePsiElement(astNode)
      else -> throw IllegalStateException("Node not supported: $astNode")
    }

  override fun createFile(viewProvider: FileViewProvider): PsiFile =
    ConventionalCommitPsiFile(viewProvider)
}
