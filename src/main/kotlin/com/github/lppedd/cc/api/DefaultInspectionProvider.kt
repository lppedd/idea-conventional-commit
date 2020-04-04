package com.github.lppedd.cc.api

import com.github.lppedd.cc.inspection.CommitBaseInspection
import com.github.lppedd.cc.inspection.CommitFormatInspection

/**
 * @author Edoardo Luppi
 */
private class DefaultInspectionProvider : CommitInspectionProvider {
  override fun getInspections(): Collection<CommitBaseInspection> =
    listOf(CommitFormatInspection())
}
