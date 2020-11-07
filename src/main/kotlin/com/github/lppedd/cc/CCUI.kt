package com.github.lppedd.cc

import com.intellij.ui.ColorUtil.isDark
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color

/**
 * @author Edoardo Luppi
 */
object CCUI {
  @JvmField
  val BorderColor: Color = JBColor {
    if (JBColor.isBright()) {
      JBColor.border()
    } else {
      val borderColor = JBColor.border()
      if (isDark(borderColor)) UIUtil.getListBackground().brighter(0.75) else borderColor
    }
  }

  @JvmField
  val ListBackgroundColor: Color = JBColor {
    if (JBColor.isBright()) {
      UIUtil.getListBackground()
    } else {
      UIUtil.getListBackground().brighter(0.96)
    }
  }
}
