package com.github.lppedd.cc.language

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.BODY
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.BREAKING_CHANGE
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.FOOTER_TYPE
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.FOOTER_VALUE
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.SCOPE
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.SUBJECT
import com.github.lppedd.cc.language.ConventionalCommitSyntaxHighlighter.Companion.TYPE
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitColorSettingsPage : ColorSettingsPage {
  private companion object {
    private val descriptors = arrayOf(
        AttributesDescriptor(CCBundle["cc.language.token.type"], TYPE),
        AttributesDescriptor(CCBundle["cc.language.token.scope"], SCOPE),
        AttributesDescriptor(CCBundle["cc.language.token.breakingChange"], BREAKING_CHANGE),
        AttributesDescriptor(CCBundle["cc.language.token.subject"], SUBJECT),
        AttributesDescriptor(CCBundle["cc.language.token.body"], BODY),
        AttributesDescriptor(CCBundle["cc.language.token.footerType"], FOOTER_TYPE),
        AttributesDescriptor(CCBundle["cc.language.token.footerValue"], FOOTER_VALUE),
    )
  }

  override fun getIcon(): Icon =
    CCIcons.Logo

  override fun getHighlighter(): SyntaxHighlighter =
    ConventionalCommitSyntaxHighlighter()

  override fun getDemoText(): String =
    """
      refactor(video)!: improve output quality
      
      The entire code of the stream processor
      has been rewritten to improve the overall quality
      
      BREAKING-CHANGE: compatibility with releases up to 2.0
        has been dropped
      Closes: #16
    """.trimIndent()

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? =
    null

  override fun getAttributeDescriptors(): Array<AttributesDescriptor> =
    descriptors

  override fun getColorDescriptors(): Array<ColorDescriptor> =
    arrayOf()

  override fun getDisplayName(): String =
    ConventionalCommitLanguage.displayName
}
