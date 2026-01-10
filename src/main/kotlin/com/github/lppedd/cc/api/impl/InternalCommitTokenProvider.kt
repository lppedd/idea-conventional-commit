package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCNotificationService
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.configuration.CCTokensService
import com.github.lppedd.cc.configuration.CCTokensService.TokensModel
import com.github.lppedd.cc.configuration.CoAuthorsResult
import com.github.lppedd.cc.configuration.TokensResult
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
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

  override fun getId(): String =
    ID

  override fun getPresentation(): ProviderPresentation =
    DefaultProviderPresentation

  override fun getCommitTypes(prefix: String): Collection<CommitType> =
    getTokens().types.map { (key, value) -> DefaultCommitToken(key, value.description) }

  override fun getCommitScopes(type: String): Collection<CommitScope> {
    val defaultType = getTokens().types[type] ?: return emptyList()
    return defaultType.scopes.map { DefaultCommitToken(it.name, it.description) }
  }

  override fun getCommitFooterTypes(): Collection<CommitFooterType> =
    getTokens().footerTypes.map { (key, value) -> DefaultCommitToken(key, value.description) }

  override fun getCommitFooterValues(
    footerType: String,
    type: String?,
    scope: String?,
    subject: String?,
  ): Collection<CommitFooterValue> {
    if ("co-authored-by".equals(footerType, ignoreCase = true)) {
      val tokensService = project.service<CCTokensService>()

      when (val result = tokensService.getCoAuthors()) {
        is CoAuthorsResult.Success -> {
          return result.coAuthors.take(3).map { DefaultCommitToken(it, "", true) }
        }
        is CoAuthorsResult.Failure -> {
          logger.debug("Error while getting co-authors", result.message)
        }
      }
    }

    val defaultFooterType = getTokens().footerTypes[footerType] ?: return emptyList()
    return defaultFooterType.values.map { DefaultCommitToken(it.name, it.description) }
  }

  private fun getTokens(): TokensModel {
    val tokensService = project.service<CCTokensService>()

    @Suppress("LoggingSimilarMessage")
    return when (val result = tokensService.getTokens()) {
      is TokensResult.Success -> result.tokens
      is TokensResult.FileError -> {
        logger.error("Error while getting tokens", result.message)
        tokensService.getBundledTokens()
      }
      is TokensResult.SchemaError -> {
        logger.debug("Error while getting tokens", result.failure)
        val details = CCBundle["cc.notifications.schema.validation"]
        val message = CCBundle["cc.notifications.schema", details]
        project.service<CCNotificationService>().notifyError(message)
        tokensService.getBundledTokens()
      }
    }
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
