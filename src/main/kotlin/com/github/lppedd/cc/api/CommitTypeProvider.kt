package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus

internal val TYPE_EP = ProjectExtensionPointName<CommitTypeProvider>(
  "com.github.lppedd.idea-conventional-commit.commitTypeProvider"
)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
interface CommitTypeProvider : CommitTokenProvider {
  fun getCommitTypes(prefix: String?): Collection<CommitType>
}

open class CommitType @JvmOverloads constructor(
    text: String,
    description: String = "",
    value: String = text,
) : CommitTokenElement(text, description, value)
