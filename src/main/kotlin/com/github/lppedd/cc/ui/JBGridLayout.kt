package com.github.lppedd.cc.ui

import com.github.lppedd.cc.scaled
import java.awt.GridLayout

/**
 * @author Edoardo Luppi
 */
class JBGridLayout(rows: Int, cols: Int, hgap: Int, vgap: Int) :
    GridLayout(
        rows,
        cols,
        hgap.scaled,
        vgap.scaled
    )
