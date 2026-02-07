package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CCRegistry
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.FooterTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.vcs.log.VcsCommitMetadata
import kotlin.math.max
import kotlin.text.RegexOption.MULTILINE

/**
 * @author Edoardo Luppi
 */
internal class InternalRecentCommitsService(private val project: Project) : RecentCommitsService {
  private companion object {
    private const val MAX_ELEMENTS = 4

    /**
     * Regexp to match beginning and ending whitespace characters,
     * including tabs and new lines.
     */
    private val regexBeginEndWs = Regex("""^\s+|\s+$""")
    private val regexBlankLines = Regex("""^\s*$""", MULTILINE)
  }

  private val types = LinkedHashSet<String>(16)
  private val scopes = LinkedHashSet<String>(16)
  private val subjects = LinkedHashSet<String>(16)
  private val footerValues = LinkedHashSet<String>(16)

  init {
    val vcsService = VcsService.getInstance(project)
    vcsService.addListener(object : VcsService.VcsListener {
      override fun onRefresh() {
        updateCachedTokens()
      }
    })

    updateCachedTokens()
  }

  override fun getRecentTypes(): Collection<String> =
    types

  override fun getRecentScopes(): Collection<String> =
    scopes

  override fun getRecentSubjects(): Collection<String> =
    subjects

  override fun getRecentFooterValues(): Collection<String> =
    footerValues

  override fun getLocalMessageHistory(): Collection<String> {
    val vcsConfiguration = VcsConfiguration.getInstance(project)
    return vcsConfiguration.recentMessages
  }

  override fun clearLocalMessageHistory() {
    val vcsConfiguration = VcsConfiguration.getInstance(project)
    vcsConfiguration.myLastCommitMessages.clear()
    updateCachedTokens()
  }

  private fun updateCachedTokens() {
    types.clear()
    scopes.clear()
    subjects.clear()
    footerValues.clear()

    getOrderedSavedCommitMessages().forEach(::parseMessage)
    getOrderedVcsCommitMessages().forEach(::parseMessage)
  }

  private fun getOrderedVcsCommitMessages(): Collection<String> {
    if (!CCRegistry.isVcsSupportEnabled()) {
      return emptyList()
    }

    val vcsHandler = VcsService.getInstance(project)
    val currentUsers = vcsHandler.getCurrentUsers()
    return vcsHandler.getOrderedTopCommits()
      .asSequence()
      .filter { currentUsers.contains(it.author) }
      .map(VcsCommitMetadata::getFullMessage)
      .toList()
  }

  private fun getOrderedSavedCommitMessages(): Collection<String> {
    val vcsConfiguration = VcsConfiguration.getInstance(project)
    return vcsConfiguration.recentMessages.asReversed().take(25)
  }

  private fun parseMessage(message: String) {
    val lines = message.lines()
    val firstValidLine = lines.firstOrNull(String::isNotBlank) ?: return
    val headerLine = firstValidLine.lowercase()
    val (type, scope, _, _, subject) = CCParser.parseHeader(headerLine)

    if (type is ValidToken && types.size < MAX_ELEMENTS) {
      types.add(type.value.trim())
    }

    if (scope is ValidToken && scopes.size < MAX_ELEMENTS) {
      scopes.add(scope.value.trim())
    }

    if (subject is ValidToken && subjects.size < MAX_ELEMENTS) {
      subjects.add(subject.value.trim())
    }

    val values = message.replace(regexBeginEndWs, "")
      .split(regexBlankLines)
      .drop(1)
      .asSequence()
      .map { it.replace(regexBeginEndWs, "") }
      .filter(String::isNotBlank)
      .map(CCParser::parseFooter)
      .filter { (t) -> t is ValidToken }
      .map(FooterTokens::footer)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .take(max(MAX_ELEMENTS - footerValues.size, 0))
      .map(String::trim)
      .toList()

    footerValues.addAll(values)
  }
}
