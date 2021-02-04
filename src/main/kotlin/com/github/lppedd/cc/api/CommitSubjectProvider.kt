package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus.*

@JvmSynthetic
internal val SUBJECT_EP = ProjectExtensionPointName<CommitSubjectProvider>(
    "com.github.lppedd.idea-conventional-commit.commitSubjectProvider"
)

/**
 * @author Edoardo Luppi
 */
@Experimental
interface CommitSubjectProvider : CommitTokenProvider {
  fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject>
}

open class CommitSubject(text: String, value: String = text) : CommitTokenElement(text, "", value)
