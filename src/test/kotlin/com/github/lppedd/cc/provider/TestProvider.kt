package com.github.lppedd.cc.provider

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.*
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal object TestProvider :
    CommitSubjectProvider,
    CommitBodyProvider,
    CommitFooterTypeProvider,
    CommitFooterValueProvider {
  override fun getCommitSubjects(type: String, scope: String): Collection<CommitSubject> =
    listOf(
      TestCommitToken("subject one"),
      TestCommitToken("and subject two"),
    )

  override fun getCommitBodies(type: String, scope: String, subject: String): Collection<CommitBody> =
    listOf(
      TestCommitToken("Example body"),
      TestCommitToken("Example of a\nmultiline body\nas it spawns multiple lines."),
      TestCommitToken("Example of a\nmultiline body\n\nwith double new line."),
    )

  override fun getCommitFooterTypes(): Collection<CommitFooterType> =
    emptyList()

  override fun getCommitFooterValues(
    footerType: String,
    type: String?,
    scope: String?,
    subject: String?,
  ): Collection<CommitFooterValue> =
    listOf(
      TestCommitToken("Footer one"),
      TestCommitToken("Footer two"),
      TestCommitToken("Long footer three\nwhich spawns\n\nmultiple lines.\n"),
    )

  override fun getId(): String =
    "test-body-provider"

  override fun getPresentation(): ProviderPresentation =
    TestProviderPresentation

  private object TestProviderPresentation : ProviderPresentation {
    override fun getName(): String =
      "com.github.lppedd.cc.provider.TestBodyProvider"

    override fun getIcon(): Icon =
      CC.Icon.Logo
  }

  private class TestCommitToken(private val text: String) :
      CommitSubject,
      CommitBody,
      CommitFooterType,
      CommitFooterValue {
    override fun getText(): String =
      text

    override fun getValue(): String =
      getText()

    override fun getDescription(): String =
      ""

    override fun getPresentation(): TokenPresentation =
      object : TokenPresentation {}
  }
}
