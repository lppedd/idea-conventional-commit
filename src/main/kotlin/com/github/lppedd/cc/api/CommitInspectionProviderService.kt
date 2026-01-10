package com.github.lppedd.cc.api

import com.intellij.openapi.components.service

/**
 * @author Edoardo Luppi
 */
public interface CommitInspectionProviderService {
  public companion object {
    @JvmStatic
    public fun getInstance(): CommitInspectionProviderService = service()
  }

  /**
   * Returns all the registered commit inspection providers.
   */
  public fun getInspectionProviders(): Collection<CommitInspectionProvider>
}
