package com.github.lppedd.cc.ui

import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.scaled
import com.intellij.ide.ui.laf.darcula.DarculaUIUtil
import com.intellij.ide.ui.laf.darcula.ui.DarculaEditorTextFieldBorder
import com.intellij.ui.ComponentUtil
import com.intellij.ui.EditorTextField
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.MacUIUtil
import java.awt.*
import java.awt.geom.Path2D
import java.awt.geom.Path2D.Float
import java.awt.geom.Rectangle2D
import javax.swing.border.Border

/**
 * In case Darcula is being used, paint the `EditorTextField` border
 * even when the component is disabled.
 *
 * Also, enhances the visual representation of the (optional) vertical scrollbar
 * by moving it closer to the side border.
 *
 * @author Edoardo Luppi
 */
@Compatibility(description = "See IDEA-262020")
internal class FixedEditorTextFieldBorder(private val delegatedBorder: Border) : Border {
  override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
    delegatedBorder.paintBorder(c, g, x, y, width, height)

    if (delegatedBorder !is DarculaEditorTextFieldBorder) {
      return
    }

    val editorTextField = ComponentUtil.getParentOfType(EditorTextField::class.java, c)

    if (editorTextField == null || editorTextField.isEnabled) {
      return
    }

    val g2 = g.create() as Graphics2D

    try {
      val hintValue = if (MacUIUtil.USE_QUARTZ) {
        RenderingHints.VALUE_STROKE_PURE
      } else {
        RenderingHints.VALUE_STROKE_NORMALIZE
      }

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, hintValue)

      val r = Rectangle(x, y, width, height)
      JBInsets.removeFrom(r, JBUI.insets(1))
      g2.translate(r.x, r.y)

      val lw = DarculaUIUtil.LW.float
      val bw = DarculaUIUtil.BW.float
      val outer = Rectangle2D.Float(bw, bw, r.width - bw * 2, r.height - bw * 2)

      g2.color = c.background
      g2.fill(outer)

      val border = Float(Path2D.WIND_EVEN_ODD)
      border.append(outer, false)
      border.append(
          Rectangle2D.Float(bw + lw, bw + lw, r.width - (bw + lw) * 2, r.height - (bw + lw) * 2),
          false,
      )

      g2.color = DarculaUIUtil.getOutlineColor(false, false)
      g2.fill(border)
    } finally {
      g2.dispose()
    }
  }

  override fun getBorderInsets(c: Component?): Insets =
    delegatedBorder.getBorderInsets(c).also {
      it.right = 6.scaled
    }

  override fun isBorderOpaque(): Boolean =
    delegatedBorder.isBorderOpaque
}
