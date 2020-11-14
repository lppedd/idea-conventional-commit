package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import org.jetbrains.annotations.ApiStatus.*
import kotlin.text.RegexOption.MULTILINE

/**
 * @author Edoardo Luppi
 */
@Internal
internal class DefaultVcsCommitSubjectProvider(private val project: Project)
  : CommitSubjectProvider,
    CommitFooterValueProvider {
  override fun getId(): String = ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("Default - VCS", CCIcons.Logo)

  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    VcsConfiguration.getInstance(project)
      .recentMessages
      .asReversed()
      .asSequence()
      .take(30)
      .map { it.lines().first(String::isNotBlank) }
      .map(CCParser::parseHeader)
      .map(CommitTokens::subject)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .map(String::trim)
      .filter(String::isNotEmpty)
      .map(::CommitSubject)
      .toList()

  override fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue> =
    VcsConfiguration.getInstance(project)
      .recentMessages
      .asReversed()
      .asSequence()
      .take(if ("co-authored-by".equals(footerType, true)) 5 else 15)
      .flatMap { message -> getFooterValues(footerType, message) }
      .distinctBy(String::toLowerCase)
      .map(::CommitFooterValue)
      .toList()

  private fun getFooterValues(footerType: String, message: String): Sequence<String> =
    message.replace(regexBeginEndWs, "")
      .split(regexBlankLines)
      .drop(1)
      .asReversed()
      .asSequence()
      .map { it.replace(regexBeginEndWs, "") }
      .filter(String::isNotBlank)
      .map(CCParser::parseFooter)
      .filter { footerType == (it.type as? ValidToken)?.value }
      .map(FooterTokens::footer)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .map(String::trim)
      .filter(String::isNotEmpty)

  companion object {
    const val ID: String = "f3be5600-71b8-401c-bf50-e2465d8efca8"

    private val regexBeginEndWs = Regex("""^\s+|\s+$""")
    private val regexBlankLines = Regex("""^\s*$""", MULTILINE)
  }
}
