package com.github.lppedd.cc.api

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
public interface CommitInspectionProviderService {
  public companion object {
    @JvmStatic
    public fun getInstance(project: Project): CommitInspectionProviderService = project.service()
  }

  /**
   * Returns all the registered commit inspection providers.
   */
  public fun getInspectionProviders(): Collection<CommitInspectionProvider>
}
