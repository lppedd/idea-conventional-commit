package com.github.lppedd.cc.configuration.component

import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener

/**
 * @author Edoardo Luppi
 */
internal class ActionLinkLabel<T>(label: String, listener: LinkListener<T>) : LinkLabel<T>(label, null) {
  init {
    setListener(listener, null)
  }
}
