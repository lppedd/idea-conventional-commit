package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

/**
 * @author Edoardo Luppi
 */
interface CommitTypeProvider : CommitTokenProvider {
  companion object {
    internal val EP_NAME = ProjectExtensionPointName<CommitTypeProvider>(
      "com.github.lppedd.idea-conventional-commit.commitTypeProvider"
    )
  }

  fun getCommitTypes(prefix: String?): List<CommitType>

  open class CommitType(
    val text: String,
    val description: String? = null
  ) : CommitTokenElement()
}
