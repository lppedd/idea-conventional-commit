package com.github.lppedd.cc.ui

import com.intellij.ui.scale.JBUIScale
import java.awt.GridLayout

/**
 * @author Edoardo Luppi
 */
class JBGridLayout(rows: Int, cols: Int, hgap: Int, vgap: Int) :
    GridLayout(
      rows,
      cols,
      JBUIScale.scale(hgap),
      JBUIScale.scale(vgap)
    )
