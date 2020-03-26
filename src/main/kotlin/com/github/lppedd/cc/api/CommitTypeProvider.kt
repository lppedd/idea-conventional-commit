package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

internal val TYPE_EP = ProjectExtensionPointName<CommitTypeProvider>(
  "com.github.lppedd.idea-conventional-commit.commitTypeProvider"
)

/**
 * @author Edoardo Luppi
 */
interface CommitTypeProvider : CommitTokenProvider {
  fun getCommitTypes(prefix: String?): Collection<CommitType>
}

open class CommitType(
    val text: String,
    val description: String? = null,
) : CommitTokenElement()
