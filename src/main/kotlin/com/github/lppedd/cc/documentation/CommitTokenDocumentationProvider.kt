package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.api.CommitBody
import com.github.lppedd.cc.api.CommitFooter
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psiElement.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

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
      is CommitFooterPsiElement -> generateDocForFooter(element.commitFooter)
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

  private fun generateDocForFooter(element: CommitFooter): String =
    generateHtml(element.text, element.description)

  private fun generateDocForBody(element: CommitBody): String =
    generateHtml(element.text, element.description)

  private fun generateHtml(text: String, description: String): String {
    val sb = StringBuilder(text.length + description.length + 140)

    if (description.isNotBlank()) {
      sb.append(DocumentationMarkup.CONTENT_START)
        .append(description)
        .append(DocumentationMarkup.CONTENT_END)
    }

    return sb
      .append(DocumentationMarkup.SECTIONS_START)
      .append(DocumentationMarkup.SECTION_HEADER_START)
      .append("Text:")
      .append(DocumentationMarkup.SECTION_SEPARATOR)
      .append("<p>")
      .append(text)
      .append(DocumentationMarkup.SECTION_END)
      .append(DocumentationMarkup.SECTIONS_END)
      .toString()
  }
}
