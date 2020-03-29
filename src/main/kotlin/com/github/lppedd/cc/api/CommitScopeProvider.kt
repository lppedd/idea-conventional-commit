package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

internal val SCOPE_EP = ProjectExtensionPointName<CommitScopeProvider>(
  "com.github.lppedd.idea-conventional-commit.commitScopeProvider"
)

/**
 * @author Edoardo Luppi
 */
interface CommitScopeProvider : CommitTokenProvider {
  fun getCommitScopes(commitType: String?): Collection<CommitScope>
}

open class CommitScope @JvmOverloads constructor(
    val text: String,
    val description: String = "",
) : CommitTokenElement()
