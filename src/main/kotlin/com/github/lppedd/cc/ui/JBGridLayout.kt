package com.github.lppedd.cc.ui

import com.intellij.util.ui.JBUI
import java.awt.GridLayout

/**
 * @author Edoardo Luppi
 */
class JBGridLayout(rows: Int, cols: Int, hgap: Int, vgap: Int) :
  GridLayout(
    rows,
    cols,
    JBUI.scale(hgap),
    JBUI.scale(vgap)
  )
