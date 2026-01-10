package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.api.CommitInspectionProvider
import com.github.lppedd.cc.api.CommitInspectionProviderService
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile

/**
 * @author Edoardo Luppi
 */
internal class CommitInspectionsRegistrarStartupActivity : ProjectActivity, DumbAware {
  private object Lock {
    // Used to synchronize the inspections registration process
    val lock = Any()
  }

  override suspend fun execute(project: Project) {
    val inspectionProviderService = CommitInspectionProviderService.getInstance()
    val inspectionProviders = inspectionProviderService.getInspectionProviders()
    val inspections = inspectionProviders.flatMap(CommitInspectionProvider::getInspections)

    if (inspections.isNotEmpty()) {
      synchronized(Lock.lock) {
        val inspectionProfile = CommitMessageInspectionProfile.getInstance(project)

        for (inspection in inspections) {
          inspectionProfile.addTool(project, LocalInspectionToolWrapper(inspection), emptyMap())
        }
      }
    }
  }
}
