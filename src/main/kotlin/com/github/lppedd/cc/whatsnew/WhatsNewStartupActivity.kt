package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.api.WHATS_NEW_EP
import com.github.lppedd.cc.api.WhatsNewProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.concurrency.EdtScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Verifies the need to display the What's New dialog at startup.
 *
 * @author Edoardo Luppi
 * @see [WhatsNewDialog]
 */
private class WhatsNewStartupActivity : StartupActivity, DumbAware {
  override fun runActivity(project: Project) {
    if (System.getProperty("com.github.lppedd.cc.whatsnew.disable") != null) {
      return
    }

    val shouldDisplay = WHATS_NEW_EP.extensions
      .asSequence()
      .filter(WhatsNewProvider::shouldDisplay)
      .any { it.files.fileDescriptions.isNotEmpty() }

    if (shouldDisplay) {
      EdtScheduledExecutorService.getInstance().schedule({
        if (!project.isDisposed) {
          WhatsNewDialog.showForProject(project)
        }
      }, 1100L, TimeUnit.MILLISECONDS)
    }
  }
}
