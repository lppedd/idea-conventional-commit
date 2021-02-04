package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus.*

@JvmSynthetic
internal val BODY_EP = ProjectExtensionPointName<CommitBodyProvider>(
    "com.github.lppedd.idea-conventional-commit.commitBodyProvider"
)

/**
 * @author Edoardo Luppi
 */
@Experimental
@AvailableSince("0.8.0")
interface CommitBodyProvider : CommitTokenProvider {
  fun getCommitBodies(
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitBody>
}

open class CommitBody @JvmOverloads constructor(
    text: String,
    description: String = "",
    value: String = text,
) : CommitTokenElement(text, description, value)
