package com.github.lppedd.cc.language

import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.language.lexer.ConventionalCommitLexer
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.HighlighterColors.TEXT
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import java.util.*
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey as attrs

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitSyntaxHighlighter(project: Project?) : SyntaxHighlighterBase() {
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
      it[ConventionalCommitTokenType.FOOTER_TYPE_BREAKING_CHANGE] = attrsBreakingChange
      it[ConventionalCommitTokenType.FOOTER_VALUE] = attrsFooterValue
    }
  }

  private val configService = project?.let { CCConfigService.getInstance(it) }

  override fun getHighlightingLexer(): Lexer =
    ConventionalCommitLexer()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
    if (configService == null || configService.enableLanguageSupport) {
      attrsMap.getOrDefault(tokenType, attrsText)
    } else {
      TextAttributesKey.EMPTY_ARRAY
    }
}
