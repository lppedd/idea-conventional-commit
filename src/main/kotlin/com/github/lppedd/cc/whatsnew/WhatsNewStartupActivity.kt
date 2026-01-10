package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.api.WhatsNewProvider
import com.github.lppedd.cc.api.WhatsNewProviderService
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.delay

/**
 * Verifies the need to display the What's New dialog at startup.
 *
 * @author Edoardo Luppi
 * @see [WhatsNewDialog]
 */
internal class WhatsNewStartupActivity : ProjectActivity, DumbAware {
  override suspend fun execute(project: Project) {
    if (System.getProperty("com.github.lppedd.cc.whatsnew.disable") != null) {
      return
    }

    val shouldDisplay = WhatsNewProviderService.getInstance().getWhatsNewProviders()
      .filter(WhatsNewProvider::shouldDisplayAtStartup)
      .any { it.getPages().isNotEmpty() }

    if (shouldDisplay) {
      delay(1100L)

      if (!project.isDisposed) {
        WhatsNewDialog.showForProject(project)
      }
    }
  }
}
