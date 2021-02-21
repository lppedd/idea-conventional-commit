package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psiElement.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.UIUtil

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
    buildTokenDoc(type, CCBundle["cc.completion.documentation.definition.type"])

  private fun buildScopeDoc(scope: CommitScope): String =
    buildTokenDoc(scope, CCBundle["cc.completion.documentation.definition.scope"])

  private fun buildSubjectDoc(subject: CommitSubject): String =
    buildTokenDoc(subject, CCBundle["cc.completion.documentation.definition.subject"])

  private fun buildBodyDoc(body: CommitBody): String =
    buildTokenDoc(body, CCBundle["cc.completion.documentation.definition.body"])

  private fun buildFooterTypeDoc(footerType: CommitFooterType): String =
    buildTokenDoc(footerType, CCBundle["cc.completion.documentation.definition.footerType"])

  private fun buildFooterValueDoc(footerValue: CommitFooterValue): String =
    buildTokenDoc(footerValue, CCBundle["cc.completion.documentation.definition.footerValue"])

  private fun buildTokenDoc(token: CommitTokenElement, definition: String): String =
    buildHtml(definition, token.description.trim(), token.value.trim())

  private fun buildHtml(definition: String, description: String, value: String): String {
    val totalLength = value.length + description.length
    val sb = StringBuilder(if (totalLength == 0) return "" else totalLength + 140)

    val grayedColorHex = ColorUtil.toHex(UIUtil.getContextHelpForeground())
    sb.append(DocumentationMarkup.DEFINITION_START)
      .append("<span style='font-size: 90%; color: #$grayedColorHex'>$definition</span>")
      .append(DocumentationMarkup.DEFINITION_END)

    sb.append(DocumentationMarkup.CONTENT_START)

    if (description.isNotEmpty()) {
      sb.append(description)
    } else {
      sb.append("<span style='color: #$grayedColorHex'>")
        .append(CCBundle["cc.completion.documentation.noDescription"])
        .append("</span>")
    }

    sb.append(DocumentationMarkup.CONTENT_END)

    if (value.isNotEmpty()) {
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
}
