package com.github.lppedd.cc

import com.github.lppedd.cc.vcs.commitbuilder.CommitBuilderService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

/**
 * @author Edoardo Luppi
 */
internal class CCProjectManagerListener : ProjectManagerListener {
  override fun projectClosingBeforeSave(project: Project) =
    CommitBuilderService.getInstance(project).clear()
}
