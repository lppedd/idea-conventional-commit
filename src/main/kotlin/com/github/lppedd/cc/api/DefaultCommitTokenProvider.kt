package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.everit.json.schema.ValidationException
import org.jetbrains.annotations.ApiStatus.*

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
  ): Collection<CommitFooterValue> =
    if ("co-authored-by".equals(footerType, true)) {
      defaultsService.getCoAuthors().take(3).map(::CommitFooterValue)
    } else {
      emptyList()
    }

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
