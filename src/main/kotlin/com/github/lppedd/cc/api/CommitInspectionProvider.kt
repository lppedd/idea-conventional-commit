package com.github.lppedd.cc.api

import com.github.lppedd.cc.inspection.CommitBaseInspection
import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitInspectionProvider {
  fun getInspections(): Collection<CommitBaseInspection>
}
