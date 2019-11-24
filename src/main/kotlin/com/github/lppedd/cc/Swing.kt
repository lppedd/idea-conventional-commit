package com.github.lppedd.cc.configuration.component

import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.Insets

/**
 * Creates a GridBagConstraints object with
 * all of its fields set to the passed-in arguments.
 */
@Suppress("FunctionName")
fun KGridBagConstraints(
  gridx: Int = GridBagConstraints.RELATIVE,
  gridy: Int = GridBagConstraints.RELATIVE,
  gridwidth: Int = 1,
  gridheight: Int = 1,
  weightx: Double = 0.0,
  weighty: Double = 0.0,
  anchor: Int = GridBagConstraints.CENTER,
  fill: Int = GridBagConstraints.NONE,
  insets: Insets = Insets(0, 0, 0, 0),
  ipadx: Int = 0,
  ipady: Int = 0
) = GridBagConstraints(
  gridx,
  gridy,
  gridwidth,
  gridheight,
  weightx,
  weighty,
  anchor,
  fill,
  insets,
  ipadx,
  ipady
)

@Suppress("FunctionName")
fun KGridConstraints(
  row: Int = 0,
  column: Int = 0,
  rowSpan: Int = 1,
  colSpan: Int = 1,
  vSizePolicy: Int = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK,
  hSizePolicy: Int = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK,
  fill: Int = GridConstraints.FILL_NONE,
  anchor: Int = GridConstraints.ANCHOR_CENTER,
  minimumSize: Dimension = Dimension(-1, -1),
  preferredSize: Dimension = Dimension(-1, -1),
  maximumSize: Dimension = Dimension(-1, -1),
  indent: Int = 0,
  useParentLayout: Boolean = false
) = GridConstraints(
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
