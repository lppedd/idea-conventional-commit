package com.github.lppedd.cc.ui

import com.github.lppedd.cc.ui.CCDialogWrapper.ValidationNavigable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.EditorTextField
import java.awt.Component

/**
 * @author Edoardo Luppi
 */
internal abstract class DialogGotoErrorAction : DumbAwareAction() {
  init {
    isEnabledInModalContext = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.EDT

  override fun update(event: AnActionEvent) {
    event.presentation.isEnabledAndVisible = event.getData(ValidationNavigable.DIALOG) != null
  }

  protected fun findFocusableComponent(component: Component?): Component? =
    when (component) {
      is EditorTextField -> component.focusTarget
      else -> component
    }
}
