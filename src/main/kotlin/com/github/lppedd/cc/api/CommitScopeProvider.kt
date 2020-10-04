package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus

internal val SCOPE_EP = ProjectExtensionPointName<CommitScopeProvider>(
  "com.github.lppedd.idea-conventional-commit.commitScopeProvider"
)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
interface CommitScopeProvider : CommitTokenProvider {
  fun getCommitScopes(commitType: String?): Collection<CommitScope>
}

open class CommitScope @JvmOverloads constructor(
    text: String,
    description: String = "",
    value: String = text,
) : CommitTokenElement(text, description, value)
