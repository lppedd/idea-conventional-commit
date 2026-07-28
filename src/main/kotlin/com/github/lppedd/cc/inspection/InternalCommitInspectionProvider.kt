package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.api.CommitInspectionProvider
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile

/**
 * @author Edoardo Luppi
 */
internal class InternalCommitInspectionProvider(private val project: Project) : CommitInspectionProvider {
  override fun getInspections(): Collection<CommitBaseInspection> {
    val inspections = mutableListOf(
      CommitFormatInspection(),
      CommitNamingConventionInspection(),
    )

    val inspectionProfile = CommitMessageInspectionProfile.getInstance(project)
    val hasSpellCheckingInspection = inspectionProfile.tools.any {
      it.tool.tool::class.java.name == "com.intellij.vcs.commit.message.CommitMessageSpellCheckingInspection"
    }

    if (!hasSpellCheckingInspection) {
      inspections += CommitMessageSpellCheckingInspection()
    }

    return inspections
  }
}
