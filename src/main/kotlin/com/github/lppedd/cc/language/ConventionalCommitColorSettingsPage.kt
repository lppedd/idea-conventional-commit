package com.github.lppedd.cc.language

import com.github.lppedd.cc.CCIcons
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
        AttributesDescriptor("Type", ConventionalCommitSyntaxHighlighter.TYPE),
        AttributesDescriptor("Scope", ConventionalCommitSyntaxHighlighter.SCOPE),
        AttributesDescriptor("Breaking change", ConventionalCommitSyntaxHighlighter.BREAKING_CHANGE),
        AttributesDescriptor("Subject", ConventionalCommitSyntaxHighlighter.SUBJECT),
        AttributesDescriptor("Body", ConventionalCommitSyntaxHighlighter.BODY),
        AttributesDescriptor("Footer type", ConventionalCommitSyntaxHighlighter.FOOTER_TYPE),
        AttributesDescriptor("Footer value", ConventionalCommitSyntaxHighlighter.FOOTER_VALUE),
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
