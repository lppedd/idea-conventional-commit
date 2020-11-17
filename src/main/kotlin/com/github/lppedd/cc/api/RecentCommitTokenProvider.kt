package com.github.lppedd.cc.api

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitTokenElement.CommitTokenRendering
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterTokens
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.vcs.CCVcsHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import org.jetbrains.annotations.ApiStatus.*
import kotlin.text.RegexOption.MULTILINE

/**
 * Provides the most recently used commit tokens.
 *
 * @author Edoardo Luppi
 */
@Internal
internal class RecentCommitTokenProvider(project: Project)
  : CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider,
    CommitFooterValueProvider {
  companion object {
    const val ID: String = "f3be5600-71b8-401c-bf50-e2465d8efca8"
    const val MAX_ELEMENTS = 3

    private val regexBeginEndWs = Regex("""^\s+|\s+$""")
    private val regexBlankLines = Regex("""^\s*$""", MULTILINE)
    private val recentCommitRendering = CommitTokenRendering(type = "Recently used")
  }

  private val vcsHandler = project.service<CCVcsHandler>()
  private val vcsConfiguration = VcsConfiguration.getInstance(project)

  override fun getId(): String = ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("Recently used", CCIcons.Provider.Recent)

  override fun getCommitTypes(prefix: String?): Collection<CommitType> =
    doGet(::RecentCommitType) { messages ->
      messages.map { it.lines().first(String::isNotBlank) }
        .mapToLowerCase()
        .distinct()
        .map(CCParser::parseHeader)
        .filter { it.type is ValidToken && it.separator.isPresent }
        .map { it.type as ValidToken }
        .map(ValidToken::value)
        .trim()
        .filterNotEmpty()
        .toMutableSet()
    }

  override fun getCommitScopes(commitType: String?): Collection<CommitScope> =
    doGet(::RecentCommitScope) { messages ->
      messages.map { it.lines().first(String::isNotBlank) }
        .mapToLowerCase()
        .distinct()
        .map(CCParser::parseHeader)
        .map(CommitTokens::scope)
        .filterIsInstance<ValidToken>()
        .map(ValidToken::value)
        .trim()
        .filterNotEmpty()
        .toMutableSet()
    }

  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    doGet(::RecentCommitSubject) { messages ->
      messages.map { it.lines().first(String::isNotBlank) }
        .distinctBy(String::toLowerCase)
        .map(CCParser::parseHeader)
        .map(CommitTokens::subject)
        .filterIsInstance<ValidToken>()
        .map(ValidToken::value)
        .trim()
        .filterNotEmpty()
        .distinctBy(String::toLowerCase)
        .toMutableSet()
    }

  override fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue> =
    doGet(::RecentCommitFooterValue) { messages ->
      messages.flatMap(::getFooterValues)
        .distinctBy(String::toLowerCase)
        .toMutableSet()
    }

  private fun getFooterValues(message: String): Sequence<String> =
    message.replace(regexBeginEndWs, "")
      .split(regexBlankLines)
      .drop(1)
      .asSequence()
      .map { it.replace(regexBeginEndWs, "") }
      .filterNotBlank()
      .map(CCParser::parseFooter)
      .filter { it.type is ValidToken }
      .map(FooterTokens::footer)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()

  private fun <T : CommitTokenElement> doGet(
      ctor: (String) -> T,
      transformation: (Sequence<String>) -> MutableSet<String>,
  ): Collection<T> {
    val tokens = transformation(getOrderedSavedCommitMessages())

    // If we've already found all the required tokens in the saved messages
    // we don't need to query the VCS log, it would only slow down completion
    if (tokens.size < MAX_ELEMENTS) {
      val remainingElements = MAX_ELEMENTS - tokens.size
      tokens.addAll(transformation(getOrderedVcsCommitMessages()).take(remainingElements))
    }

    return tokens.take(MAX_ELEMENTS).map(ctor)
  }

  private fun getOrderedVcsCommitMessages(): Sequence<String> =
    vcsHandler.getOrderedTopCommits()
      .asSequence()
      .map { it.fullMessage }

  private fun getOrderedSavedCommitMessages(): Sequence<String> =
    vcsConfiguration.recentMessages
      .asReversed()
      .asSequence()
      .take(25)

  private class RecentCommitType(text: String) : CommitType(text) {
    override fun getRendering() = recentCommitRendering
  }

  private class RecentCommitScope(text: String) : CommitScope(text) {
    override fun getRendering() = recentCommitRendering
  }

  private class RecentCommitSubject(text: String) : CommitSubject(text) {
    override fun getRendering() = recentCommitRendering
  }

  private class RecentCommitFooterValue(text: String) : CommitFooterValue(text) {
    override fun getRendering() = recentCommitRendering
  }
}
