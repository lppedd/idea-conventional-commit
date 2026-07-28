package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.api.CommitInspectionProvider
import com.github.lppedd.cc.api.CommitInspectionProviderService
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class InternalCommitInspectionProviderService(private val project: Project) : CommitInspectionProviderService {
  private companion object {
    private val inspectionEpName: ProjectExtensionPointName<CommitInspectionProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitInspectionProvider")
  }

  override fun getInspectionProviders(): Collection<CommitInspectionProvider> =
    inspectionEpName.getExtensions(project)
}
