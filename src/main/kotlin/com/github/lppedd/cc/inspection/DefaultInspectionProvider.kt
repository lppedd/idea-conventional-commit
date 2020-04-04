package com.github.lppedd.cc.inspection

/**
 * @author Edoardo Luppi
 */
private class DefaultInspectionProvider : CommitInspectionProvider {
  override fun getInspections(): Collection<ConventionalCommitBaseInspection> =
    listOf(CommitFormatInspection())
}
