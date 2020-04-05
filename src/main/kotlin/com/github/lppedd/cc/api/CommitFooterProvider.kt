package com.github.lppedd.cc.api

import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus

internal val FOOTER_EP = ProjectExtensionPointName<CommitFooterProvider>(
  "com.github.lppedd.idea-conventional-commit.commitFooterProvider"
)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince("0.8.0")
interface CommitFooterProvider : CommitTokenProvider {
  fun getCommitFooterTypes(): Collection<CommitFooterType>
  fun getCommitFooters(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooter>
}

open class CommitFooterType @JvmOverloads constructor(
    @get:JvmName("getText")
    val value: String,
    val description: String = "",
) : CommitTokenElement()

open class CommitFooter @JvmOverloads constructor(
    @get:JvmName("getText")
    val value: String,
    val description: String = "",
) : CommitTokenElement()
