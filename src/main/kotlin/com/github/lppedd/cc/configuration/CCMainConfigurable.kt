package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
private class CCMainConfigurable(private val project: Project) : SearchableConfigurable {
  private val defaultsService = project.service<CCDefaultTokensService>()
  private val configService = project.service<CCConfigService>()
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

    val tokens = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      defaultsService.getBuiltInDefaults()
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

    try {
      val tokens = defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
      gui.setTokens(tokens.types)
    } catch (e: Exception) {
      gui.revalidate()
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
