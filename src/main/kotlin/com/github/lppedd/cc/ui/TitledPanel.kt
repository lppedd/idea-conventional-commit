package com.github.lppedd.cc.ui

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class TitledPanel(title: String, component: Component) : JPanel(BorderLayout(0, JBUI.scale(5))) {
  init {
    add(JBLabel(title), BorderLayout.NORTH)
    add(component, BorderLayout.CENTER)
  }
}
