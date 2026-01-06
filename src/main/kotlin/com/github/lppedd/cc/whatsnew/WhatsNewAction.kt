package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CC
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

/**
 * Displays a dialog containing plugin's changelogs.
 *
 * @author Edoardo Luppi
 * @see [WhatsNewDialog]
 */
internal class WhatsNewAction : DumbAwareAction() {
  init {
    templatePresentation.icon = CC.Icon.Logo
  }

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.EDT

  override fun actionPerformed(event: AnActionEvent) {
    WhatsNewDialog.showForProject(event.project ?: return)
  }
}
