package com.github.lppedd.cc.action

import com.intellij.openapi.editor.actions.TabAction

/**
 * @author Edoardo Luppi
 */
internal class CommitTabAction : TabAction() {
  init {
    setupHandler(CommitTabHandler())
  }
}
