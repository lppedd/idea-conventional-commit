package com.github.lppedd.cc.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
@ExtensionPoint
interface ConventionalCommitProvider {
  fun getCommitTypes(): List<CommitType>
  fun getCommitScopes(commitType: String?): List<CommitScope>
  fun getCommitSubjects(project: Project, commitType: String?, commitScope: String? = null): List<CommitSubject>

  companion object {
    val EP_NAME = ExtensionPointName.create<ConventionalCommitProvider>(
      "com.github.lppedd.idea-conventional-commit.conventionalCommitProvider"
    )
  }
}
