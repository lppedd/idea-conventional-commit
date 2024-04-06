package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.moveCaretToOffset
import com.github.lppedd.cc.removeSelection
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.ObjectUtils

/**
 * Opens the [CommitBuilderDialog] dialog and sets a new commit message in the editor
 * using the user-inputted values (the previous message gets entirely overwritten).
 *
 * @author Edoardo Luppi
 */
internal class CommitBuilderAction : DumbAwareAction() {
  init {
    isEnabledInModalContext = true
    templatePresentation.icon = CCIcons.Logo
  }

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.EDT

  override fun update(event: AnActionEvent) {
    val isVisible = event.project != null && getCommitMessagePanel(event) != null
    event.presentation.isEnabledAndVisible = isVisible
  }

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val commitMessagePanel = getCommitMessagePanel(event) ?: return
    val commitBuilderDialog = CommitBuilderDialog(project)

    if (!commitBuilderDialog.showAndGet()) {
      return
    }

    val command = {
      val commitMessage = commitBuilderDialog.getCommitMessage()
      commitMessagePanel.setCommitMessage(commitMessage)

      if (commitMessagePanel is CommitMessage) {
        // Remove the selection and move the caret at the beginning
        // to avoid the user accidentally deleting all the text
        commitMessagePanel.editorField.editor?.let {
          it.removeSelection(true)
          it.moveCaretToOffset(0)
        }
      }
    }

    CommandProcessor.getInstance().executeCommand(
        project,
        command,
        CCBundle["cc.commitbuilder.title"],
        ObjectUtils.sentinel("CommitBuilder"),
    )
  }

  private fun getCommitMessagePanel(event: AnActionEvent): CommitMessageI? =
    event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
}
