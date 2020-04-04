package com.github.lppedd.cc

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.border.Border
import kotlin.internal.InlineOnly

@InlineOnly
internal inline fun Border.concat(border: Border): Border =
  BorderFactory.createCompoundBorder(this, border)

@InlineOnly
internal inline fun gridConstraints(
    row: Int = 0,
    column: Int = 0,
    rowSpan: Int = 1,
    colSpan: Int = 1,
    vSizePolicy: Int = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK,
    hSizePolicy: Int = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK,
    fill: Int = FILL_NONE,
    anchor: Int = ANCHOR_CENTER,
    minimumSize: Dimension = Dimension(-1, -1),
    preferredSize: Dimension = Dimension(-1, -1),
    maximumSize: Dimension = Dimension(-1, -1),
    indent: Int = 0,
    useParentLayout: Boolean = false,
) =
  GridConstraints(
    row,
    column,
    rowSpan,
    colSpan,
    anchor,
    fill,
    hSizePolicy,
    vSizePolicy,
    minimumSize,
    preferredSize,
    maximumSize,
    indent,
    useParentLayout
  )
