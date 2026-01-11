package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitToken
import com.github.lppedd.cc.api.impl.DefaultTokenPresentation
import com.github.lppedd.cc.brighter
import com.github.lppedd.cc.darker
import com.github.lppedd.cc.scaled
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.LookupElementDocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CommitTokenLookupElementDocumentationTargetProvider : LookupElementDocumentationTargetProvider {
  override fun documentationTarget(psiFile: PsiFile, element: LookupElement, offset: Int): DocumentationTarget? {
    var lookupElement = element

    if (element is DelegatingLookupElement<*>) {
      lookupElement = element.getDelegate()
    }

    if (lookupElement is CommitTokenLookupElement) {
      return CommitTokenDocumentationTarget(lookupElement)
    }

    return null
  }

  private class CommitTokenDocumentationTarget(val element: CommitTokenLookupElement) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> =
      Pointer.hardPointer(this)

    override fun computePresentation(): TargetPresentation =
      TargetPresentation.builder(element.getItemText())
        .icon(element.psiElement.getIcon(false))
        .presentation()

    override fun computeDocumentation(): DocumentationResult? {
      val title = when (element) {
        is CommitTypeLookupElement -> CCBundle["cc.completion.documentation.definition.type"]
        is CommitScopeLookupElement -> CCBundle["cc.completion.documentation.definition.scope"]
        is CommitSubjectLookupElement -> CCBundle["cc.completion.documentation.definition.subject"]
        is CommitBodyLookupElement -> CCBundle["cc.completion.documentation.definition.body"]
        is CommitFooterTypeLookupElement -> CCBundle["cc.completion.documentation.definition.footerType"]
        is CommitFooterValueLookupElement -> CCBundle["cc.completion.documentation.definition.footerValue"]
        else -> return null
      }

      val html = buildHtml(element.getToken(), title)
      return DocumentationResult.documentation(html)
    }

    @NlsSafe
    private fun buildHtml(token: CommitToken, title: String): String {
      val value = token.getValue().trim()
      val description = token.getDescription()?.trim() ?: ""
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

      val presentation = token.getPresentation() ?: DefaultTokenPresentation
      val hasCustomDocumentation = description.isNotEmpty() && presentation.hasCustomDocumentation()

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
          .append(value.replace("\n", "<br/>"))
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
  }
}
