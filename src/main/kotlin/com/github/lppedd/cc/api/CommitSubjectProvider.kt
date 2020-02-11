package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

/**
 * @author Edoardo Luppi
 */
interface CommitSubjectProvider : CommitTokenProvider {
  companion object {
    internal val EP_NAME = ProjectExtensionPointName<CommitSubjectProvider>(
      "com.github.lppedd.idea-conventional-commit.commitSubjectProvider"
    )
  }

  fun getCommitSubjects(commitType: String?, commitScope: String?): List<CommitSubject>

  open class CommitSubject(val text: String) : CommitTokenElement()
}
