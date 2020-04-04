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
    val standardTools = super.createTools(project)
    val element = LocalInspectionToolWrapper(CommitFormatInspection())
    return standardTools.plus(element)
  }
}
