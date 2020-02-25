package com.github.lppedd.cc.api

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitScopeProvider.CommitScope
import com.github.lppedd.cc.api.CommitSubjectProvider.CommitSubject
import com.github.lppedd.cc.api.CommitTypeProvider.CommitType
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import org.everit.json.schema.ValidationException

/**
 * @author Edoardo Luppi
 */
internal class DefaultCommitTokenProvider(private val project: Project)
  : CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider {
  private val configService = CCConfigService.getInstance(project)
  private val defaultsService = CCDefaultTokensService.getInstance(project)

  override fun getId() = CCConstants.DEFAULT_PROVIDER_ID
  override fun getPresentation() = ProviderPresentation("Default", CCIcons.DEFAULT_PRESENTATION)

  override fun getCommitTypes(prefix: String?): List<CommitType> =
    getDefaults().map { CommitType(it.key, it.value.description) }

  override fun getCommitScopes(commitType: String?) =
    when (commitType) {
      null -> emptyList()
      else -> getDefaults()[commitType]?.scopes?.map { CommitScope(it.key, it.value.description) }
              ?: emptyList()
    }

  override fun getCommitSubjects(commitType: String?, commitScope: String?): List<CommitSubject> =
    getRecentVcsMessages(project)

  private fun getDefaults() =
    try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      val error =
        if (e is ValidationException) {
          val messages = e.allMessages.joinToString("<br />", " <br />")
          CCBundle["cc.notifications.schema"] + messages
        } else {
          CCBundle["cc.notifications.schema"]
        }

      CCNotificationService
        .createErrorNotification(error)
        .notify(project)
      defaultsService.getBuiltInDefaults()
    }

  private fun getRecentVcsMessages(project: Project) =
    VcsConfiguration.getInstance(project).recentMessages
      .asReversed()
      .asSequence()
      .take(20)
      .map(CCParser::parseText)
      .map(PCommitTokens::subject)
      .map(PCommitSubject::value)
      .map(String::trim)
      .filter(String::isNotEmpty)
      .map(::CommitSubject)
      .toList()
}
