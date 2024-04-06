package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.brighter
import com.github.lppedd.cc.darker
import com.github.lppedd.cc.lookupElement.CommitTokenLookupElement
import com.github.lppedd.cc.psiElement.*
import com.github.lppedd.cc.scaled
import com.intellij.lang.documentation.AbstractDocumentationProvider
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
internal class CommitTokenDocumentationProvider : AbstractDocumentationProvider() {
  private val lineSeparatorRegex = Regex("\r\n|\n|\r")

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

    if (totalLength == 0) {
      return ""
    }

    val grayedColorHex = ColorUtil.toHex(UIUtil.getContextHelpForeground())
    val sb = StringBuilder(totalLength + 200)

    val px2 = "${2.scaled}px"
    val px4 = "${4.scaled}px"

    // See DocumentationMarkup for the actual HTML elements used by default
    sb.append("<div class='content'")

    if (hasCustomDocumentation) {
      sb.append(" style='padding: 0;'>")
    } else {
      sb.append('>')
      sb.append("<div style='color: #$grayedColorHex; margin-bottom: $px4;'>$title</div>")
    }

    if (description.isNotEmpty()) {
      sb.append(description)
    } else {
      sb.append("<span style='color: #$grayedColorHex;'>")
        .append(CCBundle["cc.completion.documentation.noDescription"])
        .append("</span>")
    }

    sb.append("</div>")

    if (value.isNotEmpty()) {
      if (hasCustomDocumentation) {
        // Emulate a styled <hr>, which is still unsupported in JEditorPane
        val colorHex = ColorUtil.toHex(getSeparatorColor())
        sb.append("<div style='margin: $px4 0 $px2; font-size: 0; border-top: thin solid #$colorHex;'></div>")
      }

      sb.append("<table class='sections' style='margin-top: $px4;'>")
        .append("<tr>")
        .append("<td class='section'>")
        .append("<span style='color: #$grayedColorHex;'>")
        .append(CCBundle["cc.completion.documentation.section.value"])
        .append("</span>")
        .append("</td>")
        .append("<td>")
        .append(value.replace(lineSeparatorRegex, "<br/>"))
        .append("</td>")
        .append("</tr>")
        .append("</table>")
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
