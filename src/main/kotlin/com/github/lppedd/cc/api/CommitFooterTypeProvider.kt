package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus

internal val FOOTER_TYPE_EP = ProjectExtensionPointName<CommitFooterTypeProvider>(
  "com.github.lppedd.idea-conventional-commit.commitFooterTypeProvider"
)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince("0.11.0")
interface CommitFooterTypeProvider : CommitTokenProvider {
  fun getCommitFooterTypes(): Collection<CommitFooterType>
}

open class CommitFooterType @JvmOverloads constructor(
    text: String,
    description: String = "",
    value: String = text,
) : CommitTokenElement(text, description, value)
