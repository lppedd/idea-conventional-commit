package com.github.lppedd.cc.ui

import com.github.lppedd.cc.ui.CCDialogWrapper.ValidationNavigable
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Moves the focus to the next component that has a validation error.
 *
 * @author Edoardo Luppi
 */
internal class DialogGotoNextErrorAction : DialogGotoErrorAction() {
  override fun actionPerformed(event: AnActionEvent) {
    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    val dialog = event.getData(ValidationNavigable.DIALOG)!!
    val errors = dialog.validateAll()

    if (errors.isEmpty()) {
      return
    }

    val focusIndex = errors.indexOfFirst {
      findFocusableComponent(it.component)?.hasFocus() == true
    }

    if (focusIndex < 0 || focusIndex == errors.lastIndex) {
      errors[0].component?.requestFocus()
    } else {
      errors[focusIndex + 1].component?.requestFocus()
    }
  }
}
