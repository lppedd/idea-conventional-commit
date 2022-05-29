package com.github.lppedd.cc.api

/**
 * Provides inspections for the commit message panel.
 *
 * Inspections may be customized via
 * `Settings > Version Control > Commit > Commit Message Inspections`.
 *
 * @author Edoardo Luppi
 */
interface CommitInspectionProviderService {
  /** Returns all the registered commit inspections' providers. */
  fun getInspectionProviders(): Collection<CommitInspectionProvider>
}
