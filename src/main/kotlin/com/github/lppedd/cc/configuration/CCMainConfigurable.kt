package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
internal class CCMainConfigurable(private val project: Project) : SearchableConfigurable {
  private companion object {
    private val logger = logger<CCMainConfigurable>()
  }

  private val tokensService = CCTokensService.getInstance(project)
  private val configService = CCConfigService.getInstance(project)
  private val disposable = Disposer.newDisposable("CCMainConfigurable")
  private lateinit var gui: CCMainConfigurableGui

  override fun getId(): String =
    "preferences.${CC.AppName}"

  override fun getDisplayName(): String =
    CCBundle["cc.plugin.name"]

  override fun createComponent(): JComponent {
    gui = CCMainConfigurableGui(project, disposable)
    gui.completionType = configService.completionType
    gui.isEnableLanguageSupport = configService.enableLanguageSupport
    gui.isPrioritizeRecentlyUsed = configService.prioritizeRecentlyUsed
    gui.isAutoInsertSpaceAfterColon = configService.autoInsertSpaceAfterColon
    gui.customTokensFilePath = configService.customFilePath
    gui.customCoAuthorsFilePath = configService.customCoAuthorsFilePath

    @Suppress("LoggingSimilarMessage")
    val tokens = when (val result = tokensService.getTokens()) {
      is TokensResult.Success -> result.tokens
      is TokensResult.FileError -> {
        logger.debug("Error while getting tokens", result.message)
        tokensService.getBundledTokens()
      }
      is TokensResult.SchemaError -> {
        logger.debug("Error while getting tokens", result.failure)
        tokensService.getBundledTokens()
      }
    }

    gui.setTokens(tokens.types)
    return gui.rootPanel
  }

  override fun isModified(): Boolean =
    gui.isValid && (
        gui.completionType != configService.completionType ||
        gui.isEnableLanguageSupport != configService.enableLanguageSupport ||
        gui.isPrioritizeRecentlyUsed != configService.prioritizeRecentlyUsed ||
        gui.isAutoInsertSpaceAfterColon != configService.autoInsertSpaceAfterColon ||
        gui.customCoAuthorsFilePath != configService.customCoAuthorsFilePath ||
        gui.customTokensFilePath != configService.customFilePath)

  override fun apply() {
    configService.completionType = gui.completionType
    configService.enableLanguageSupport = gui.isEnableLanguageSupport
    configService.prioritizeRecentlyUsed = gui.isPrioritizeRecentlyUsed
    configService.autoInsertSpaceAfterColon = gui.isAutoInsertSpaceAfterColon
    configService.customCoAuthorsFilePath = gui.customCoAuthorsFilePath
    configService.customFilePath = gui.customTokensFilePath

    @Suppress("LoggingSimilarMessage")
    when (val result = tokensService.getTokens()) {
      is TokensResult.Success -> gui.setTokens(result.tokens.types)
      is TokensResult.FileError -> {
        logger.debug("Error while getting tokens", result.message)
        gui.revalidate()
      }
      is TokensResult.SchemaError -> {
        logger.debug("Error while getting tokens", result.failure)
        gui.revalidate()
      }
    }

    // Notify that settings have been changed
    val publisher = project.messageBus.syncPublisher(ConfigurationChangedListener.TOPIC)
    publisher.onConfigurationChanged()
  }

  override fun reset() {
    gui.completionType = configService.completionType
    gui.isEnableLanguageSupport = configService.enableLanguageSupport
    gui.isPrioritizeRecentlyUsed = configService.prioritizeRecentlyUsed
    gui.isAutoInsertSpaceAfterColon = configService.autoInsertSpaceAfterColon
    gui.customCoAuthorsFilePath = configService.customCoAuthorsFilePath
    gui.customTokensFilePath = configService.customFilePath
  }

  override fun disposeUIResources() {
    Disposer.dispose(disposable)
  }
}
