package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus.*

internal val FOOTER_VALUE_EP = ProjectExtensionPointName<CommitFooterValueProvider>(
  "com.github.lppedd.idea-conventional-commit.commitFooterValueProvider"
)

/**
 * @author Edoardo Luppi
 */
@Experimental
@AvailableSince("0.11.0")
interface CommitFooterValueProvider : CommitTokenProvider {
  fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue>
}

open class CommitFooterValue @JvmOverloads constructor(
    text: String,
    description: String = "",
    value: String = text,
) : CommitTokenElement(text, description, value)
