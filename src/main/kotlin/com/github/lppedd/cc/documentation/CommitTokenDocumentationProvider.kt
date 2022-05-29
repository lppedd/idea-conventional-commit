package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.brighter
import com.github.lppedd.cc.darker
import com.github.lppedd.cc.lookupElement.CommitTokenLookupElement
import com.github.lppedd.cc.psiElement.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.FakePsiElement
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
private class CommitTokenDocumentationProvider : AbstractDocumentationProvider() {
  private val lineSeparatorRegex = Regex("\r\n|\n\r|\n|\r")

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? =
    when (element) {
      is CommitTokenDocumentationElement -> generateCommitTokenDoc(element)
      else -> super.generateDoc(element, originalElement)
    }

  override fun getDocumentationElementForLookupItem(
      psiManager: PsiManager?,
      obj: Any?,
      element: PsiElement,
  ): PsiElement? =
    if (obj is CommitTokenLookupElement) {
      CommitTokenDocumentationElement(obj.getToken(), obj.psiElement, element)
    } else {
      super.getDocumentationElementForLookupItem(psiManager, obj, element)
    }

  private fun generateCommitTokenDoc(element: CommitTokenDocumentationElement): String? {
    val title = when (element.tokenPsiElement) {
      is CommitTypePsiElement -> CCBundle["cc.completion.documentation.definition.type"]
      is CommitScopePsiElement -> CCBundle["cc.completion.documentation.definition.scope"]
      is CommitSubjectPsiElement -> CCBundle["cc.completion.documentation.definition.subject"]
      is CommitBodyPsiElement -> CCBundle["cc.completion.documentation.definition.body"]
      is CommitFooterTypePsiElement -> CCBundle["cc.completion.documentation.definition.footerType"]
      is CommitFooterValuePsiElement -> CCBundle["cc.completion.documentation.definition.footerValue"]
      else -> return null
    }

    return buildHtml(element.token, title)
  }

  private fun buildHtml(token: CommitToken, title: String): String {
    val description = token.getDescription().trim()
    val value = token.getValue().trim()
    val hasCustomDocumentation = description.isNotEmpty() && token.getPresentation().hasCustomDocumentation()
    val totalLength = value.length + description.length
    val sb = StringBuilder(if (totalLength == 0) return "" else totalLength + 140)
    val grayedColorHex = ColorUtil.toHex(UIUtil.getContextHelpForeground())
    sb.append("<div style='border-bottom: none; padding: 4px 7px 2px'>")
      .append("<span style='color: #$grayedColorHex'>$title</span>")
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
        .append(value.replace(lineSeparatorRegex, "<br/>"))
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

  private class CommitTokenDocumentationElement(
      val token: CommitToken,
      val tokenPsiElement: CommitTokenPsiElement,
      private val parent: PsiElement,
  ) : FakePsiElement() {
    override fun getParent(): PsiElement =
      parent

    override fun getPresentableText(): String? =
      tokenPsiElement.presentableText

    override fun getIcon(open: Boolean): Icon? =
      tokenPsiElement.getIcon(open)
  }
}
