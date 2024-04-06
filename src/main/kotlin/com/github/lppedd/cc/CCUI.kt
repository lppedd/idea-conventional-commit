package com.github.lppedd.cc

import com.intellij.ui.ColorUtil.isDark
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color

/**
 * @author Edoardo Luppi
 */
public object CCUI {
  @JvmField
  public val BorderColor: Color = JBColor.lazy {
    if (JBColor.isBright()) {
      JBColor.border()
    } else {
      val borderColor = JBColor.border()

      if (isDark(borderColor)) {
        UIUtil.getListBackground().brighter(0.75)
      } else {
        borderColor
      }
    }
  }

  @JvmField
  public val ListBackgroundColor: Color = JBColor.lazy {
    if (JBColor.isBright()) {
      UIUtil.getListBackground()
    } else {
      UIUtil.getListBackground().brighter(0.96)
    }
  }
}
