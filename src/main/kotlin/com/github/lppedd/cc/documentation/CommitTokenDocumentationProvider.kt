package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.brighter
import com.github.lppedd.cc.darker
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psiElement.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color

/**
 * @author Edoardo Luppi
 */
private class CommitTokenDocumentationProvider : AbstractDocumentationProvider() {
  private val lineSeparatorRegex = Regex("\r\n|\n\r|\n|\r")

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    if (element !is CommitFakePsiElement) {
      return null
    }

    val description = when (element) {
      is CommitTypePsiElement -> buildTypeDoc(element.commitType)
      is CommitScopePsiElement -> buildScopeDoc(element.commitScope)
      is CommitSubjectPsiElement -> buildSubjectDoc(element.commitSubject)
      is CommitBodyPsiElement -> buildBodyDoc(element.commitBody)
      is CommitFooterTypePsiElement -> buildFooterTypeDoc(element.commitFooterType)
      is CommitFooterValuePsiElement -> buildFooterValueDoc(element.commitFooterValue)
      else -> null
    }

    // TODO: maybe create a CommitTokenDocumentationProvider API
    return description?.ifBlank { null }
  }

  override fun getDocumentationElementForLookupItem(
      psiManager: PsiManager?,
      obj: Any?,
      element: PsiElement?,
  ) = if (obj is CommitLookupElement) {
    obj.psiElement
  } else {
    super.getDocumentationElementForLookupItem(psiManager, obj, element)
  }

  private fun buildTypeDoc(type: CommitType): String =
    buildHtml(type, CCBundle["cc.completion.documentation.definition.type"])

  private fun buildScopeDoc(scope: CommitScope): String =
    buildHtml(scope, CCBundle["cc.completion.documentation.definition.scope"])

  private fun buildSubjectDoc(subject: CommitSubject): String =
    buildHtml(subject, CCBundle["cc.completion.documentation.definition.subject"])

  private fun buildBodyDoc(body: CommitBody): String =
    buildHtml(body, CCBundle["cc.completion.documentation.definition.body"])

  private fun buildFooterTypeDoc(footerType: CommitFooterType): String =
    buildHtml(footerType, CCBundle["cc.completion.documentation.definition.footerType"])

  private fun buildFooterValueDoc(footerValue: CommitFooterValue): String =
    buildHtml(footerValue, CCBundle["cc.completion.documentation.definition.footerValue"])

  private fun buildHtml(token: CommitTokenElement, definition: String): String {
    val description = token.description.trim()
    val value = token.value.trim()
    val hasCustomDocumentation = description.isNotEmpty() && token.getRendering().hasCustomDocumentation
    val totalLength = value.length + description.length
    val sb = StringBuilder(if (totalLength == 0) return "" else totalLength + 140)

    val grayedColorHex = ColorUtil.toHex(UIUtil.getContextHelpForeground())
    sb.append("<div style='border-bottom: none; padding: 4px 7px 2px; font-style: italic'>")
      .append("<span style='font-size: 90%; color: #$grayedColorHex'>$definition</span>")
      .append("</div>")

    // See DocumentationMarkup.CONTENT_START
    sb.append("<div class='content'")

    if (hasCustomDocumentation) {
      sb.append(" style='padding: 0'>")
    } else {
      sb.append('>')
    }

    if (description.isNotEmpty()) {
      sb.append(description)
    } else {
      sb.append("<span style='color: #$grayedColorHex'>")
        .append(CCBundle["cc.completion.documentation.noDescription"])
        .append("</span>")
    }

    sb.append(DocumentationMarkup.CONTENT_END)

    if (value.isNotEmpty()) {
      if (hasCustomDocumentation) {
        // Emulate a styled <hr>, which is still unsupported in JEditorPane
        val colorHex = ColorUtil.toHex(getSeparatorColor())
        sb.append("<div style='margin: 4px 0 2px; font-size: 0; border-top: thin solid #$colorHex'></div>")
      }

      sb.append(DocumentationMarkup.SECTIONS_START)
        .append(DocumentationMarkup.SECTION_HEADER_START)
        .append("<span style='color: #$grayedColorHex'>")
        .append(CCBundle["cc.completion.documentation.section.value"])
        .append("</span>")
        .append(DocumentationMarkup.SECTION_SEPARATOR)
        .append(value.replace(lineSeparatorRegex, "<br>"))
        .append(DocumentationMarkup.SECTION_END)
        .append(DocumentationMarkup.SECTIONS_END)
    }

    return "$sb"
  }

  private fun getSeparatorColor(): Color {
    val color = UIUtil.getTooltipSeparatorColor()
    return if (JBColor.isBright()) {
      color.brighter(0.97)
    } else {
      color.darker(0.97)
    }
  }
}
