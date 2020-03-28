package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.DEFAULT_PROVIDER_ID
import com.github.lppedd.cc.ICON_DEFAULT_PRESENTATION
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.intellij.openapi.project.Project
import org.everit.json.schema.ValidationException

private val FOOTER_TYPES = listOf(
  CommitFooterType("BREAKING CHANGE"),
  CommitFooterType("Closes"),
  CommitFooterType("Implements"),
)

/**
 * @author Edoardo Luppi
 */
private class DefaultCommitTokenProvider(private val project: Project) :
    CommitTypeProvider,
    CommitScopeProvider,
    CommitFooterProvider {
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

  override fun getCommitFooterTypes(): Collection<CommitFooterType> =
    FOOTER_TYPES

  override fun getCommitFooters(
      footerType: String,
      commitType: String?,
      commitScope: String?,
      commitSubject: String?,
  ): Collection<CommitFooter> = emptyList()

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
}
