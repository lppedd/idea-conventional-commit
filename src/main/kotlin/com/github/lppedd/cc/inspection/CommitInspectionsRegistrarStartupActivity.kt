package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.api.CommitInspectionProvider
import com.github.lppedd.cc.api.CommitInspectionProviderService
import com.github.lppedd.cc.application
import com.github.lppedd.cc.service
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile

/**
 * @author Edoardo Luppi
 */
internal class CommitInspectionsRegistrarStartupActivity : StartupActivity, DumbAware {
  override fun runActivity(project: Project) {
    val inspectionProviderService = application.service<CommitInspectionProviderService>()
    val inspections = inspectionProviderService.getInspectionProviders()
      .asSequence()
      .flatMap(CommitInspectionProvider::getInspections)
      .map(::LocalInspectionToolWrapper)
      .toList()

    if (inspections.isNotEmpty()) {
      val inspectionProfile = CommitMessageInspectionProfile.getInstance(project)

      for (inspection in inspections) {
        inspectionProfile.addTool(project, inspection, emptyMap())
      }
    }
  }
}
