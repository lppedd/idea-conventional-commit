@file:JvmName("CommitGotoActions")

package com.github.lppedd.cc.editor

import com.github.lppedd.cc.isCommitMessage
import com.intellij.codeInsight.daemon.impl.actions.GotoNextErrorAction
import com.intellij.codeInsight.daemon.impl.actions.GotoPreviousErrorAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vcs.ui.Refreshable

/**
 * Allows forward navigation of inspections results with key shortcuts
 * inside the commit message editor.
 *
 * @author Edoardo Luppi
 */
private class CommitGotoNextErrorAction : GotoNextErrorAction() {
  init {
    isEnabledInModalContext = true
  }

  override fun update(event: AnActionEvent) {
    super.update(event)
    updateActionState(event)
  }
}

/**
 * Allows backward navigation of inspections results with key shortcuts
 * inside the commit message editor.
 *
 * @author Edoardo Luppi
 */
private class CommitGotoPreviousErrorAction : GotoPreviousErrorAction() {
  init {
    isEnabledInModalContext = true
  }

  override fun update(event: AnActionEvent) {
    super.update(event)
    updateActionState(event)
  }
}

private fun updateActionState(event: AnActionEvent) {
  when {
    isInCommitMessagePanel(event) -> {
      // We know we are inside the modal commit dialog.
      // The action should be enabled only for the commit editor
      // and not for other editor fields (e.g. Author)
      event.presentation.isEnabled = isInCommitMessageEditor(event)
    }

    // We might be inside any context, for example the non-modal commit workflow.
    // The action behavior should respect the original one: disabled in modal context
    event.getData(PlatformDataKeys.IS_MODAL_CONTEXT) == true -> {
      event.presentation.isEnabled = false
    }
  }
}

private fun isInCommitMessagePanel(event: AnActionEvent): Boolean =
  event.getData(Refreshable.PANEL_KEY) != null

private fun isInCommitMessageEditor(event: AnActionEvent): Boolean =
  event.getData(CommonDataKeys.EDITOR)?.document?.isCommitMessage() ?: false
