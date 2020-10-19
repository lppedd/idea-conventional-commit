package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.ICON_DEFAULT_PRESENTATION
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
    templatePresentation.apply {
      text = CCBundle["cc.whatsnew.title"]
      icon = ICON_DEFAULT_PRESENTATION
    }
  }

  override fun actionPerformed(event: AnActionEvent) {
    WhatsNewDialog.showForProject(event.project ?: return)
  }
}
