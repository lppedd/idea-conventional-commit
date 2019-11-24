package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

/**
 * @author Edoardo Luppi
 */
interface CommitScopeProvider : CommitTokenProvider {
  companion object {
    val EP_NAME = ProjectExtensionPointName<CommitScopeProvider>(
      "com.github.lppedd.idea-conventional-commit.commitScopeProvider"
    )
  }

  fun getCommitScopes(commitType: String?): List<CommitScope>

  open class CommitScope(
    val text: String,
    val description: String? = null
  ) : CommitTokenElement()
}
