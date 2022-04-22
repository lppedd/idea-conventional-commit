package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

/**
 * Displays a dialog containing plugin's changelogs.
 *
 * @author Edoardo Luppi
 * @see [WhatsNewDialog]
 */
private class WhatsNewAction : DumbAwareAction() {
  init {
    templatePresentation.icon = CCIcons.Logo
  }

  override fun actionPerformed(event: AnActionEvent) {
    WhatsNewDialog.showForProject(event.project ?: return)
  }
}
