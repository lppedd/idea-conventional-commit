package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCBundle
import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Component
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

    val link = LinkLabel(CCBundle["cc.whatsnew.dialog.visitPage"], null, VisitPageAction()).also {
      it.alignmentX = CENTER_ALIGNMENT
      it.setPaintUnderline(false)
    }

    layout = BoxLayout(this, BoxLayout.Y_AXIS)
    add(Box.createVerticalGlue())
    add(label)
    add(link)
    add(Box.createVerticalGlue())
    border = JBUI.Borders.empty(40, 30)
  }

  private class VisitPageAction : LinkListener<Any> {
    override fun linkSelected(aSource: LinkLabel<Any>?, aLinkData: Any?) {
      BrowserUtil.browse(URI(CCBundle["cc.plugin.repository"]))
    }
  }
}
