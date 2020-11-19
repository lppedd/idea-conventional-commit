package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCBundle
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.ActionLink
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Desktop
import java.net.URI
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class WhatsNewEmptyPanel : JPanel() {
  init {
    val label = JBLabel(CCBundle["cc.whatsnew.dialog.empty"]).also {
      it.alignmentX = Component.CENTER_ALIGNMENT
      it.foreground = UIUtil.getInactiveTextColor()
    }

    val link = ActionLink(CCBundle["cc.whatsnew.dialog.visitPage"], VisitPageAction()).also {
      it.alignmentX = Component.CENTER_ALIGNMENT
      it.setPaintUnderline(false)
    }

    layout = BoxLayout(this, BoxLayout.Y_AXIS)
    add(Box.createVerticalGlue())
    add(label)
    add(link)
    add(Box.createVerticalGlue())
  }

  private class VisitPageAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
      try {
        Desktop.getDesktop().browse(URI(CCBundle["cc.plugin.repository"]))
      } catch (_: Exception) {
        //
      }
    }
  }
}
