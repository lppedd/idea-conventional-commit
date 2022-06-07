package com.github.lppedd.cc.api

/**
 * @author Edoardo Luppi
 */
interface CommitInspectionProviderService {
  /** Returns all the registered commit inspections' providers. */
  fun getInspectionProviders(): Collection<CommitInspectionProvider>
}
