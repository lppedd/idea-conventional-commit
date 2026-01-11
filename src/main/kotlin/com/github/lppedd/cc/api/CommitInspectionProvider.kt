package com.github.lppedd.cc.api

import com.github.lppedd.cc.inspection.CommitBaseInspection
import org.jetbrains.annotations.ApiStatus

/**
 * Provides inspections for the commit message panel's editor.
 *
 * Inspections may be customized via `Settings > Version Control > Commit > Commit Message Inspections`.
 *
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
public interface CommitInspectionProvider {
  public fun getInspections(): Collection<CommitBaseInspection>
}
