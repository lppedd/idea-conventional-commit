package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.configuration.SchemaValidationException
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class InternalCommitTokenProvider(private val project: Project) :
    CommitTypeProvider,
    CommitScopeProvider,
    CommitFooterTypeProvider,
    CommitFooterValueProvider {
  companion object {
    const val ID: String = "e9d4e8de-79a0-48b8-b1ba-b4161e2572c0"
    private val logger = logger<InternalCommitTokenProvider>()
  }

  private val configService = project.service<CCConfigService>()
  private val defaultsService = project.service<CCDefaultTokensService>()
  private val defaults
    get() = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: ProcessCanceledException) {
      throw e
    } catch (e: Exception) {
      logger.debug("Error while reading the custom tokens file", e)
      notifyErrorToUser(e)
      defaultsService.getBuiltInDefaults()
    }

  override fun getId(): String =
    ID

  override fun getPresentation(): ProviderPresentation =
    DefaultProviderPresentation

  override fun getCommitTypes(prefix: String): Collection<CommitType> =
    defaults.types.map { (key, value) -> DefaultCommitToken(key, value.description) }

  override fun getCommitScopes(type: String): Collection<CommitScope> {
    val defaultType = defaults.types[type] ?: return emptyList()
    return defaultType.scopes.map { DefaultCommitToken(it.name, it.description) }
  }

  override fun getCommitFooterTypes(): Collection<CommitFooterType> =
    defaults.footerTypes.map { (key, value) -> DefaultCommitToken(key, value.description) }

  override fun getCommitFooterValues(
      footerType: String,
      type: String?,
      scope: String?,
      subject: String?,
  ): Collection<CommitFooterValue> {
    if ("co-authored-by".equals(footerType, true)) {
      return defaultsService.getCoAuthors()
        .asSequence()
        .take(3)
        .map { DefaultCommitToken(it, "", true) }
        .toList()
    }

    val defaultFooterType = defaults.footerTypes[footerType] ?: return emptyList()
    return defaultFooterType.values.map { DefaultCommitToken(it.name, it.description) }
  }

  private fun notifyErrorToUser(e: Exception) {
    val details = if (e is SchemaValidationException) {
      CCBundle["cc.notifications.schema.validation"]
    } else {
      CCBundle["cc.notifications.schema.seeLogs"]
    }

    val message = CCBundle["cc.notifications.schema", details]
    CCNotificationService.createErrorNotification(message).notify(project)
  }

  private object DefaultProviderPresentation : ProviderPresentation {
    override fun getName(): String =
      CCBundle["cc.config.providers.default"]

    override fun getIcon(): Icon =
      CC.Icon.Logo
  }

  private object DefaultTokenPresentation : TokenPresentation
  private object CoAuthorTokenPresentation : TokenPresentation {
    override fun getType(): String =
      "Co-author"
  }

  private class DefaultCommitToken(
      private val text: String,
      private val description: String,
      private val isCoAuthor: Boolean = false,
  ) : CommitType,
      CommitScope,
      CommitFooterType,
      CommitFooterValue {
    override fun getText(): String =
      text

    override fun getValue(): String =
      getText()

    override fun getDescription(): String =
      description

    override fun getPresentation(): TokenPresentation =
      if (isCoAuthor) {
        CoAuthorTokenPresentation
      } else {
        DefaultTokenPresentation
      }
  }
}
