package com.github.lppedd.cc.ui

import com.github.lppedd.cc.scaled
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class TitledPanel : JPanel {
  constructor(title: String, component: Component) : super(BorderLayout(0, 5.scaled)) {
    add(JBLabel(title), BorderLayout.NORTH)
    add(component, BorderLayout.CENTER)
  }
}
