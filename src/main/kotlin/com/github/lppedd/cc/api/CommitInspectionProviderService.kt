package com.github.lppedd.cc.api

/**
 * @author Edoardo Luppi
 */
public interface CommitInspectionProviderService {
  /**
   * Returns all the registered commit inspection providers.
   */
  public fun getInspectionProviders(): Collection<CommitInspectionProvider>
}
