package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import javax.swing.Icon
import kotlin.text.RegexOption.MULTILINE

/**
 * Extracts commit tokens from the project's active VCS history.
 *
 * @author Edoardo Luppi
 */
internal class VcsCommitTokenProvider(project: Project)
  : CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider,
    CommitFooterValueProvider {
  @Suppress("CompanionObjectInExtension")
  companion object {
    const val ID = "e9ce9acf-f4a6-4b36-b43c-531169556c29"
    const val MAX_ELEMENTS = 15

    private val regexBeginEndWs = Regex("""^\s+|\s+$""")
    private val regexBlankLines = Regex("""^\s*$""", MULTILINE)
    private val specialFooterTypes = setOf(
        "author",
        "co-authored-by",
        "signed-off-by",
        "acked-by",
        "reviewed-by",
        "tested-by",
    )
  }

  private val vcsHandler = project.service<VcsService>()

  override fun getId(): String =
    ID

  override fun getPresentation(): ProviderPresentation =
    VcsProviderPresentation

  @Suppress("Destructure")
  override fun getCommitTypes(prefix: String): Collection<CommitType> =
    getOrderedVcsCommitMessages()
      .mapNotNull { it.lines().firstOrNull(String::isNotBlank) }
      .distinctBy(String::lowercase)
      .map(CCParser::parseHeader)
      .filter { it.type is ValidToken && it.separator.isPresent }
      .map { it.type as ValidToken }
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
      .distinct()
      .take(MAX_ELEMENTS)
      .map(::VcsCommitToken)
      .toList()

  override fun getCommitScopes(type: String): Collection<CommitScope> =
    getOrderedVcsCommitMessages()
      .mapNotNull { it.lines().firstOrNull(String::isNotBlank) }
      .distinctBy(String::lowercase)
      .map(CCParser::parseHeader)
      .map(CommitTokens::scope)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
      .distinct()
      .take(MAX_ELEMENTS)
      .map(::VcsCommitToken)
      .toList()

  override fun getCommitSubjects(type: String, scope: String): Collection<CommitSubject> =
    getOrderedVcsCommitMessages()
      .mapNotNull { it.lines().firstOrNull(String::isNotBlank) }
      .distinctBy(String::lowercase)
      .map(CCParser::parseHeader)
      .map(CommitTokens::subject)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()
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
    return getOrderedVcsCommitMessages()
      .flatMap { getFooterValues(it, if (matchFooterType) footerType else null) }
      .distinctBy(String::lowercase)
      .take(maxElements)
      .map(::VcsCommitToken)
      .toList()
  }

  @Suppress("Destructure")
  private fun getFooterValues(message: String, footerType: String?): Sequence<String> =
    message.replace(regexBeginEndWs, "")
      .split(regexBlankLines)
      .drop(1)
      .asSequence()
      .map { it.replace(regexBeginEndWs, "") }
      .filterNotBlank()
      .map(CCParser::parseFooter)
      .filter { it.type is ValidToken && (footerType == null || it.type.value.equals(footerType, true)) }
      .map(FooterTokens::footer)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .trim()
      .filterNotEmpty()

  private fun getOrderedVcsCommitMessages(): Sequence<String> {
    if (!Registry.`is`(CC.Registry.VcsEnabled, false)) {
      return emptySequence()
    }

    return vcsHandler.getOrderedTopCommits()
      .asSequence()
      .map { it.fullMessage }
  }

  private object VcsProviderPresentation : ProviderPresentation {
    override fun getName(): String =
      "VCS"

    override fun getIcon(): Icon =
      CCIcons.Provider.Vcs
  }

  private object VcsTokenPresentation : TokenPresentation {
    override fun getType(): String =
      "VCS"
  }

  private class VcsCommitToken(private val text: String) :
      CommitType,
      CommitScope,
      CommitSubject,
      CommitFooterValue {
    override fun getText(): String =
      text

    override fun getValue(): String =
      getText()

    override fun getDescription(): String =
      ""

    override fun getPresentation() =
      VcsTokenPresentation
  }
}
