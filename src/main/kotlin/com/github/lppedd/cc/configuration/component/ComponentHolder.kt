package com.github.lppedd.cc.configuration.component

import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
internal interface ComponentHolder {
  fun getComponent(): JComponent
}
