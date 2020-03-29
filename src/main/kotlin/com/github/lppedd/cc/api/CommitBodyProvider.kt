package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

internal val BODY_EP = ProjectExtensionPointName<CommitBodyProvider>(
  "com.github.lppedd.idea-conventional-commit.commitBodyProvider"
)

/**
 * @author Edoardo Luppi
 */
interface CommitBodyProvider : CommitTokenProvider {
  fun getCommitBodies(
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitBody>
}

open class CommitBody(val text: String, val description: String = "") : CommitTokenElement()
