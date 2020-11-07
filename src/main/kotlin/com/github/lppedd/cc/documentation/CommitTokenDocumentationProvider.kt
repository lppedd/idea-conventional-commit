package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.api.CommitBody
import com.github.lppedd.cc.api.CommitFooterValue
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psiElement.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

private val LINE_SEPARATOR_REGEX = Regex("\r\n|\n|\r")

/**
 * @author Edoardo Luppi
 */
private class CommitTokenDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    val description = when (element) {
      !is CommitFakePsiElement -> null
      is CommitTypePsiElement -> element.commitType.description
      is CommitScopePsiElement -> element.commitScope.description
      is CommitBodyPsiElement -> generateDocForBody(element.commitBody)
      is CommitFooterTypePsiElement -> element.commitFooterType.description
      is CommitFooterValuePsiElement -> generateDocForFooter(element.commitFooterValue)
      else -> null
    }

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

  private fun generateDocForFooter(element: CommitFooterValue): String =
    generateHtml(element.value.trim(), element.description.trim())

  private fun generateDocForBody(element: CommitBody): String =
    generateHtml(element.value.trim(), element.description.trim())

  private fun generateHtml(value: String, description: String): String {
    val totalLength = value.length + description.length
    val sb = StringBuilder(if (totalLength == 0) return "" else totalLength + 140)

    if (description.isNotEmpty()) {
      sb.append(DocumentationMarkup.CONTENT_START)
        .append(description)
        .append(DocumentationMarkup.CONTENT_END)
    }

    if (value.isNotEmpty()) {
      sb.append(DocumentationMarkup.SECTIONS_START)
        .append(DocumentationMarkup.SECTION_HEADER_START)
        .append("Value:")
        .append(DocumentationMarkup.SECTION_SEPARATOR)
        .append("<p>")
        .append(LINE_SEPARATOR_REGEX.replace(value, "<p>"))
        .append(DocumentationMarkup.SECTION_END)
        .append(DocumentationMarkup.SECTIONS_END)
    }

    return "$sb"
  }
}
