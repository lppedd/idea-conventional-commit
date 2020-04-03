package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName

internal val SUBJECT_EP = ProjectExtensionPointName<CommitSubjectProvider>(
  "com.github.lppedd.idea-conventional-commit.commitSubjectProvider"
)

/**
 * @author Edoardo Luppi
 */
interface CommitSubjectProvider : CommitTokenProvider {
  fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject>
}

open class CommitSubject(@get:JvmName("getText") val value: String) : CommitTokenElement()
