package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.invokeLaterOnEdt
import com.intellij.openapi.application.ApplicationManager
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

  private val disposable = Disposer.newDisposable("CCMainConfigurable")
  private lateinit var gui: CCMainConfigurableGui

  override fun getId(): String =
    "preferences.${CC.AppName}"

  override fun getDisplayName(): String =
    CCBundle["cc.plugin.name"]

  @Suppress("LoggingSimilarMessage")
  override fun createComponent(): JComponent {
    val configService = CCConfigService.getInstance(project)
    gui = CCMainConfigurableGui(project, disposable)
    gui.completionType = configService.completionType
    gui.isEnableLanguageSupport = configService.enableLanguageSupport
    gui.isPrioritizeRecentlyUsed = configService.prioritizeRecentlyUsed
    gui.isAutoInsertSpaceAfterColon = configService.autoInsertSpaceAfterColon
    gui.customTokensFilePath = configService.customFilePath
    gui.customCoAuthorsFilePath = configService.customCoAuthorsFilePath

    ApplicationManager.getApplication().executeOnPooledThread {
      val tokensService = CCTokensService.getInstance(project)
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

      invokeLaterOnEdt {
        gui.setTokens(tokens.types)
      }
    }

    return gui.rootPanel
  }

  override fun isModified(): Boolean {
    val configService = CCConfigService.getInstance(project)
    return gui.isValid && (
        gui.completionType != configService.completionType ||
        gui.isEnableLanguageSupport != configService.enableLanguageSupport ||
        gui.isPrioritizeRecentlyUsed != configService.prioritizeRecentlyUsed ||
        gui.isAutoInsertSpaceAfterColon != configService.autoInsertSpaceAfterColon ||
        gui.customCoAuthorsFilePath != configService.customCoAuthorsFilePath ||
        gui.customTokensFilePath != configService.customFilePath)
  }

  @Suppress("LoggingSimilarMessage")
  override fun apply() {
    val configService = CCConfigService.getInstance(project)
    configService.completionType = gui.completionType
    configService.enableLanguageSupport = gui.isEnableLanguageSupport
    configService.prioritizeRecentlyUsed = gui.isPrioritizeRecentlyUsed
    configService.autoInsertSpaceAfterColon = gui.isAutoInsertSpaceAfterColon
    configService.customCoAuthorsFilePath = gui.customCoAuthorsFilePath
    configService.customFilePath = gui.customTokensFilePath

    ApplicationManager.getApplication().executeOnPooledThread {
      val tokensService = CCTokensService.getInstance(project)
      val result = tokensService.getTokens()

      invokeLaterOnEdt {
        when (result) {
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
      }
    }

    // Notify that settings have been changed
    project.messageBus.syncPublisher(ConfigurationChangedListener.TOPIC).onConfigurationChanged()
  }

  override fun reset() {
    val configService = CCConfigService.getInstance(project)
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
