package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.FooterTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import org.everit.json.schema.ValidationException
import org.jetbrains.annotations.ApiStatus.*
import kotlin.text.RegexOption.MULTILINE

private val BEGIN_END_WS_REGEX = Regex("""^\s+|\s+$""")
private val BLANK_LINES_REGEX = Regex("""^\s*$""", MULTILINE)

/**
 * @author Edoardo Luppi
 */
@Internal
internal class DefaultCommitTokenProvider(private val project: Project) :
    CommitTypeProvider,
    CommitScopeProvider,
    CommitFooterTypeProvider,
    CommitFooterValueProvider {
  private val configService = project.service<CCConfigService>()
  private val defaultsService = project.service<CCDefaultTokensService>()
  private val defaults
    get() = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      notifyErrorToUser(e)
      defaultsService.getBuiltInDefaults()
    }

  override fun getId(): String = ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("Default", CCIcons.Logo)

  override fun getCommitTypes(prefix: String?): Collection<CommitType> =
    defaults.types.map { CommitType(it.key, it.value.description) }

  override fun getCommitScopes(commitType: String?): Collection<CommitScope> =
    when (commitType) {
      null -> emptyList()
      else ->
        defaults.types[commitType]
          ?.scopes
          ?.map { CommitScope(it.name, it.description) }
        ?: emptyList()
    }

  override fun getCommitFooterTypes(): Collection<CommitFooterType> =
    defaults.footerTypes.map { CommitFooterType(it.name, it.description) }

  override fun getCommitFooterValues(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooterValue> {
    val isCoAuthoredBy = "co-authored-by".equals(footerType, true)
    val lastN = if (isCoAuthoredBy) 5 else 15
    val recentValues = VcsConfiguration.getInstance(project)
      .recentMessages
      .asReversed()
      .asSequence()
      .take(lastN)
      .flatMap { message -> getFooterValues(footerType, message) }
      .map(::CommitFooterValue)
      .toList()

    return if (isCoAuthoredBy) {
      recentValues
        .plus(defaultsService.getCoAuthors().take(3).map(::CommitFooterValue))
        .distinctBy { it.text.toLowerCase() }
    } else {
      recentValues.distinctBy { it.text.toLowerCase() }
    }
  }

  private fun getFooterValues(footerType: String, message: String): Sequence<String> =
    message.replace(BEGIN_END_WS_REGEX, "")
      .split(BLANK_LINES_REGEX)
      .drop(1)
      .asReversed()
      .asSequence()
      .map { it.replace(BEGIN_END_WS_REGEX, "") }
      .filter(String::isNotBlank)
      .map(CCParser::parseFooter)
      .filter { footerType == (it.type as? ValidToken)?.value }
      .map(FooterTokens::footer)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .map(String::trim)
      .filter(String::isNotEmpty)

  private fun notifyErrorToUser(e: Exception) {
    val message =
      CCBundle["cc.notifications.schema"] +
      ((e as? ValidationException)
         ?.allMessages
         ?.joinToString("<br />", " <br />") ?: "")

    CCNotificationService.createErrorNotification(message).notify(project)
  }

  companion object {
    const val ID: String = "e9d4e8de-79a0-48b8-b1ba-b4161e2572c0"
  }
}
