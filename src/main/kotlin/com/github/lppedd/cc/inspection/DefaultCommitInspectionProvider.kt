package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.api.CommitInspectionProvider
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class DefaultCommitInspectionProvider(private val project: Project) : CommitInspectionProvider {
  override fun getInspections(): Collection<CommitBaseInspection> =
    listOf(CommitFormatInspection(project))
}
