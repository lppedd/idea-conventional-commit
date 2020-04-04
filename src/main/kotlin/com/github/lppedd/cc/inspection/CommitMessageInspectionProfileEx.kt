package com.github.lppedd.cc.inspection

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile

/**
 * @author Edoardo Luppi
 */
private class CommitMessageInspectionProfileEx(project: Project) : CommitMessageInspectionProfile(project) {
  override fun createTools(project: Project?): List<InspectionToolWrapper<*, *>> {
    val additionalInspections =
      INSPECTION_EP.extensions
        .asSequence()
        .flatMap { it.getInspections().asSequence() }
        .map(::LocalInspectionToolWrapper)
        .toList()

    return super.createTools(project).plus(additionalInspections)
  }
}
