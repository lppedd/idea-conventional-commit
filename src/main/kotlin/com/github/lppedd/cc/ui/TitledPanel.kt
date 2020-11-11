package com.github.lppedd.cc.ui

import com.intellij.ui.components.JBLabel
import com.intellij.ui.scale.JBUIScale
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class TitledPanel : JPanel {
  constructor(title: String, component: Component) : super(BorderLayout(0, JBUIScale.scale(5))) {
    add(JBLabel(title), BorderLayout.NORTH)
    add(component, BorderLayout.CENTER)
  }
}
