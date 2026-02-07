package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCRegistry
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.parser.CommitFooter
import com.github.lppedd.cc.parser.CommitMessage
import com.github.lppedd.cc.parser.ParseResult
import com.github.lppedd.cc.parser.parseConventionalCommit
import com.intellij.openapi.project.Project
import com.intellij.vcs.log.VcsCommitMetadata
import javax.swing.Icon

/**
 * Extracts commit tokens from the project's active VCS history.
 *
 * @author Edoardo Luppi
 */
internal class VcsCommitTokenProvider(project: Project) :
    CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider,
    CommitFooterValueProvider {
  @Suppress("CompanionObjectInExtension")
  companion object {
    const val ID = "e9ce9acf-f4a6-4b36-b43c-531169556c29"
    const val MAX_ELEMENTS = 15

    private val specialFooterTypes = setOf(
      "author",
      "co-authored-by",
      "signed-off-by",
      "acked-by",
      "reviewed-by",
      "tested-by",
    )
  }

  private val vcsService = VcsService.getInstance(project)

  @Volatile
  private var commitMessages: List<CommitMessage>? = null
  private val commitMessagesLock = Any()

  init {
    vcsService.addListener {
      commitMessages = null
    }
  }

  override fun getId(): String =
    ID

  override fun getPresentation(): ProviderPresentation =
    VcsProviderPresentation

  override fun getCommitTypes(prefix: String): Collection<CommitType> =
    getCommitMessages()
      .map(CommitMessage::type)
      .map(String::trim)
      .distinctBy(String::lowercase)
      .take(MAX_ELEMENTS)
      .map(::VcsCommitToken)
      .toList()

  override fun getCommitScopes(type: String): Collection<CommitScope> =
    getCommitMessages()
      .mapNotNull(CommitMessage::scope)
      .map(String::trim)
      .distinctBy(String::lowercase)
      .take(MAX_ELEMENTS)
      .map(::VcsCommitToken)
      .toList()

  override fun getCommitSubjects(type: String, scope: String): Collection<CommitSubject> =
    getCommitMessages()
      .map(CommitMessage::subject)
      .map(String::trim)
      .distinctBy(String::lowercase)
      .take(MAX_ELEMENTS)
      .map(::VcsCommitToken)
      .toList()

  override fun getCommitFooterValues(
    footerType: String,
    type: String?,
    scope: String?,
    subject: String?,
  ): Collection<CommitFooterValue> {
    val matchFooterType = specialFooterTypes.contains(footerType.lowercase())
    val maxElements = if (matchFooterType) 5 else MAX_ELEMENTS
    return getCommitMessages()
      .flatMap(CommitMessage::footers)
      .filter { it.type.equals(footerType, ignoreCase = true) }
      .mapNotNull(CommitFooter::value)
      .distinctBy(String::lowercase)
      .take(maxElements)
      .map(::VcsCommitToken)
      .toList()
  }

  private fun getCommitMessages(): Sequence<CommitMessage> {
    if (!CCRegistry.isVcsSupportEnabled()) {
      return emptySequence()
    }

    var messages = commitMessages

    if (messages != null) {
      return messages.asSequence()
    }

    return synchronized(commitMessagesLock) {
      messages = commitMessages

      if (messages != null) {
        return@synchronized messages.asSequence()
      }

      val commits = vcsService.getOrderedTopCommits()
      messages = commits.asSequence()
        .map(VcsCommitMetadata::getFullMessage)
        .map(::parseConventionalCommit)
        .filterIsInstance<ParseResult.Success>()
        .map(ParseResult.Success::message)
        .toList()

      commitMessages = messages
      return@synchronized messages.asSequence()
    }
  }

  private object VcsProviderPresentation : ProviderPresentation {
    override fun getName(): String =
      "VCS"

    override fun getIcon(): Icon =
      CC.Icon.Provider.Vcs
  }

  private object VcsTokenPresentation : TokenPresentation {
    override fun getType(): String =
      "VCS"
  }

  private class VcsCommitToken(private val value: String) :
      CommitType,
      CommitScope,
      CommitSubject,
      CommitFooterValue {
    override fun getValue(): String =
      value

    override fun getText(): String =
      getValue()

    override fun getPresentation(): TokenPresentation =
      VcsTokenPresentation
  }
}
