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
    (getOrderedSavedCommitMessages(10) + getOrderedVcsCommitMessages(20))
      .map { it.lines().first(String::isNotBlank) }
      .mapToLowerCase()
      .distinct()
      .map(CCParser::parseHeader)
      .map(CommitTokens::type)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
      .distinct()
      .take(3)
      .map(::RecentCommitType)
      .toList()

  override fun getCommitScopes(commitType: String?): Collection<CommitScope> =
    (getOrderedSavedCommitMessages(10) + getOrderedVcsCommitMessages(20))
      .map { it.lines().first(String::isNotBlank) }
      .mapToLowerCase()
      .distinct()
      .map(CCParser::parseHeader)
      .map(CommitTokens::scope)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
      .distinct()
      .take(3)
      .map(::RecentCommitScope)
      .toList()

  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    (getOrderedSavedCommitMessages(10) + getOrderedVcsCommitMessages(20))
      .map { it.lines().first(String::isNotBlank) }
      .distinctBy(String::toLowerCase)
      .map(CCParser::parseHeader)
      .map(CommitTokens::subject)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
      .distinctBy(String::toLowerCase)
      .take(3)
      .map(::RecentCommitSubject)
      .toList()

  override fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue> {
    val n = if ("co-authored-by".equals(footerType, true)) 5 else 15
    return (getOrderedSavedCommitMessages(10) + getOrderedVcsCommitMessages(20))
      .flatMap { message -> getFooterValues(footerType, message) }
      .distinctBy(String::toLowerCase)
      .take(n)
      .map(::RecentCommitFooterValue)
      .toList()
  }

  private fun getFooterValues(footerType: String, message: String): Sequence<String> =
    message.replace(regexBeginEndWs, "")
      .split(regexBlankLines)
      .drop(1)
      .asReversed()
      .asSequence()
      .map { it.replace(regexBeginEndWs, "") }
      .filterNotBlank()
      .map(CCParser::parseFooter)
      .filter { footerType == (it.type as? ValidToken)?.value }
      .map(FooterTokens::footer)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()

  @Suppress("SameParameterValue")
  private fun getOrderedVcsCommitMessages(limit: Int): Sequence<String> =
    vcsHandler.getOrderedCommits(limit)
      .asSequence()
      .map { it.fullMessage }

  @Suppress("SameParameterValue")
  private fun getOrderedSavedCommitMessages(limit: Int): Sequence<String> =
    vcsConfiguration.recentMessages
      .asReversed()
      .asSequence()
      .take(limit)

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
