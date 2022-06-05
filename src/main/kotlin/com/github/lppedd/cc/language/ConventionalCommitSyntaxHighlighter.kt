package com.github.lppedd.cc.language

import com.github.lppedd.cc.language.lexer.ConventionalCommitLexer
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.HighlighterColors.TEXT
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import java.util.*
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey as attrs

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitSyntaxHighlighter : SyntaxHighlighterBase() {
  companion object {
    @JvmField val TYPE: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_TYPE", TEXT)
    @JvmField val SCOPE: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_SCOPE", TEXT)
    @JvmField val BREAKING_CHANGE: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_BREAKING_CHANGE", TEXT)
    @JvmField val SUBJECT: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_SUBJECT", TEXT)
    @JvmField val BODY: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_BODY", TEXT)
    @JvmField val FOOTER_TYPE: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_FOOTER_TYPE", TEXT)
    @JvmField val FOOTER_VALUE: TextAttributesKey = attrs("CONVENTIONAL_COMMIT_FOOTER_VALUE", TEXT)

    private val attrsType = arrayOf(TYPE)
    private val attrsScope = arrayOf(SCOPE)
    private val attrsBreakingChange = arrayOf(BREAKING_CHANGE)
    private val attrsSubject = arrayOf(SUBJECT)
    private val attrsBody = arrayOf(BODY)
    private val attrsFooterType = arrayOf(FOOTER_TYPE)
    private val attrsFooterValue = arrayOf(FOOTER_VALUE)
    private val attrsText = arrayOf(TEXT)
    private val attrsMap = IdentityHashMap<IElementType, Array<TextAttributesKey>>(6).also {
      it[ConventionalCommitTokenType.TYPE] = attrsType
      it[ConventionalCommitTokenType.SCOPE] = attrsScope
      it[ConventionalCommitTokenType.BREAKING_CHANGE] = attrsBreakingChange
      it[ConventionalCommitTokenType.SUBJECT] = attrsSubject
      it[ConventionalCommitTokenType.BODY] = attrsBody
      it[ConventionalCommitTokenType.FOOTER_TYPE] = attrsFooterType
      it[ConventionalCommitTokenType.FOOTER_VALUE] = attrsFooterValue
    }
  }

  override fun getHighlightingLexer(): Lexer =
    ConventionalCommitLexer()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
    attrsMap.getOrDefault(tokenType, attrsText)
}
