package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.DEFAULT_PROVIDER_ID
import com.github.lppedd.cc.ICON_DEFAULT_PRESENTATION
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import org.everit.json.schema.ValidationException

/**
 * @author Edoardo Luppi
 */
private class DefaultCommitTokenProvider(private val project: Project) :
    CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider {
  private val configService = CCConfigService.getInstance(project)
  private val defaultsService = CCDefaultTokensService.getInstance(project)

  override fun getId(): String =
    DEFAULT_PROVIDER_ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("Default", ICON_DEFAULT_PRESENTATION)

  override fun getCommitTypes(prefix: String?): Collection<CommitType> =
    getDefaults().map { CommitType(it.key, it.value.description) }

  override fun getCommitScopes(commitType: String?): Collection<CommitScope> =
    when (commitType) {
      null -> emptyList()
      else ->
        getDefaults()[commitType]
          ?.scopes
          ?.map { CommitScope(it.key, it.value.description) }
        ?: emptyList()
    }

  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    getRecentVcsMessages(project)

  private fun getDefaults() =
    try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      val errorMessage =
        CCBundle["cc.notifications.schema"] +
        ((e as? ValidationException)
           ?.allMessages
           ?.joinToString("<br />", " <br />") ?: "")

      CCNotificationService
        .createErrorNotification(errorMessage)
        .notify(project)

      defaultsService.getBuiltInDefaults()
    }

  private fun getRecentVcsMessages(project: Project) =
    VcsConfiguration.getInstance(project)
      .recentMessages
      .asReversed()
      .asSequence()
      .take(20)
      .map(CCParser::parseText)
      .map(CommitTokens::subject)
      .filterIsInstance(ValidToken::class.java)
      .map(ValidToken::value)
      .map(String::trim)
      .filter(String::isNotEmpty)
      .map(::CommitSubject)
      .toList()
}
