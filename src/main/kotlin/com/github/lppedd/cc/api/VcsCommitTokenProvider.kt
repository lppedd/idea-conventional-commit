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
import org.jetbrains.annotations.ApiStatus.*
import kotlin.text.RegexOption.MULTILINE

/**
 * Extracts commit tokens from the project's active VCS history.
 *
 * @author Edoardo Luppi
 */
@Internal
internal class VcsCommitTokenProvider(project: Project)
  : CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider,
    CommitFooterValueProvider {
  companion object {
    const val ID = "e9ce9acf-f4a6-4b36-b43c-531169556c29"
    const val MAX_ELEMENTS = 8

    private val regexBeginEndWs = Regex("""^\s+|\s+$""")
    private val regexBlankLines = Regex("""^\s*$""", MULTILINE)
    private val vscCommitRendering = CommitTokenRendering(type = "VCS")
  }

  private val vcsHandler = project.service<CCVcsHandler>()

  override fun getId(): String = ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("VCS", CCIcons.Provider.Vcs)

  override fun getCommitTypes(prefix: String?): Collection<CommitType> =
    getOrderedVcsCommitMessages()
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
      .take(MAX_ELEMENTS)
      .map(::VcsCommitType)
      .toList()

  override fun getCommitScopes(commitType: String?): Collection<CommitScope> =
    getOrderedVcsCommitMessages()
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
      .take(MAX_ELEMENTS)
      .map(::VcsCommitScope)
      .toList()

  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    getOrderedVcsCommitMessages()
      .map { it.lines().first(String::isNotBlank) }
      .distinctBy(String::toLowerCase)
      .map(CCParser::parseHeader)
      .map(CommitTokens::subject)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
      .distinctBy(String::toLowerCase)
      .take(MAX_ELEMENTS)
      .map(::VcsCommitSubject)
      .toList()

  override fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue> {
    val n = if ("co-authored-by".equals(footerType, true)) 5 else MAX_ELEMENTS
    return getOrderedVcsCommitMessages()
      .flatMap { message -> getFooterValues(footerType, message) }
      .distinctBy(String::toLowerCase)
      .take(n)
      .map(::VcsCommitFooterValue)
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

  private fun getOrderedVcsCommitMessages(): Sequence<String> =
    vcsHandler.getOrderedTopCommits()
      .asSequence()
      .map { it.fullMessage }

  private class VcsCommitType(text: String) : CommitType(text) {
    override fun getRendering() = vscCommitRendering
  }

  private class VcsCommitScope(text: String) : CommitScope(text) {
    override fun getRendering() = vscCommitRendering
  }

  private class VcsCommitSubject(text: String) : CommitSubject(text) {
    override fun getRendering() = vscCommitRendering
  }

  private class VcsCommitFooterValue(text: String) : CommitFooterValue(text) {
    override fun getRendering() = vscCommitRendering
  }
}
