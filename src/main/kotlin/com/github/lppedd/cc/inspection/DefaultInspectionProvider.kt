package com.github.lppedd.cc.inspection

/**
 * @author Edoardo Luppi
 */
private class DefaultInspectionProvider : CommitInspectionProvider {
  override fun getInspections(): Collection<CommitBaseInspection> =
    listOf(CommitFormatInspection())
}
