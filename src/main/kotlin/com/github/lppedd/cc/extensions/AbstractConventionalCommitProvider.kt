package com.github.lppedd.cc.extensions

import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
abstract class AbstractConventionalCommitProvider : ConventionalCommitProvider {
  override fun getCommitTypes(): List<CommitType> = emptyList()
  override fun getCommitScopes(commitType: String?): List<CommitScope> = emptyList()
  override fun getCommitSubjects(project: Project, commitType: String?, commitScope: String?): List<CommitSubject> = emptyList()
}
