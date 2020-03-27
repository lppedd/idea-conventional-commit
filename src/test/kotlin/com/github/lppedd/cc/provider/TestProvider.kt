package com.github.lppedd.cc.provider

import com.github.lppedd.cc.ICON_DEFAULT_PRESENTATION
import com.github.lppedd.cc.api.*

internal object TestProvider : CommitSubjectProvider, CommitBodyProvider, CommitFooterProvider {
  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    listOf(
      CommitSubject("subject one"),
      CommitSubject("and subject two"),
    )

  override fun getCommitBodies(
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitBody> =
    listOf(
      CommitBody("Example body"),
      CommitBody("Example of a\nmultiline body\nas it spawns multiple lines."),
      CommitBody("Example of a\nmultiline body\n\nwith double new line."),
    )

  override fun getCommitFooterTypes(): Collection<CommitFooterType> =
    emptyList()

  override fun getCommitFooters(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooter> =
    listOf(
      CommitFooter("Footer one"),
      CommitFooter("Footer two"),
      CommitFooter("Long footer three\nwhich spawns\n\nmultiple lines.\n"),
    )

  override fun getId(): String =
    "test-body-provider"

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("com.github.lppedd.cc.provider.TestBodyProvider",
      ICON_DEFAULT_PRESENTATION)
}
