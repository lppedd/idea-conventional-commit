package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.DEFAULT_PROVIDER_ID
import com.github.lppedd.cc.ICON_DEFAULT_PRESENTATION
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.intellij.openapi.project.Project
import org.everit.json.schema.ValidationException

/**
 * @author Edoardo Luppi
 */
private class DefaultCommitTokenProvider(private val project: Project) :
    CommitTypeProvider,
    CommitScopeProvider,
    CommitFooterProvider {
  private val configService = CCConfigService.getInstance(project)
  private val defaultsService = CCDefaultTokensService.getInstance(project)
  private val defaults
    get() = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      notifyErrorToUser(e)
      defaultsService.getBuiltInDefaults()
    }

  override fun getId(): String =
    DEFAULT_PROVIDER_ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("Default", ICON_DEFAULT_PRESENTATION)

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

  override fun getCommitFooters(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooter> = emptyList()

  private fun notifyErrorToUser(e: Exception) {
    val message =
      CCBundle["cc.notifications.schema"] +
      ((e as? ValidationException)
         ?.allMessages
         ?.joinToString("<br />", " <br />") ?: "")

    CCNotificationService.createErrorNotification(message).notify(project)
  }
}
