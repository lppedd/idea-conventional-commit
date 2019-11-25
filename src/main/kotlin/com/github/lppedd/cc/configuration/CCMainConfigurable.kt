package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.api.DefaultCommitTokenProvider.JsonCommitType
import com.intellij.openapi.options.Configurable.NoScroll
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
internal class CCMainConfigurable(private val project: Project) : SearchableConfigurable, NoScroll {
  private val disposable = Disposer.newDisposable()
  private val defaults = CCDefaultTokensService.getInstance(project)
  private val config = CCConfigService.getInstance(project)
  private val gui = CCMainConfigurableGui(disposable)

  override fun getId() = "preferences.${CCConstants.APP_NAME}"
  override fun getDisplayName() = CCBundle["cc.plugin.name"]

  override fun apply() {
    val customFilePath = gui.customFilePath

    config.completionType = gui.completionType
    config.customFilePath = customFilePath

    val tokens = try {
      refreshTokens(customFilePath)
    } catch (e: Exception) {
      emptyMap<String, JsonCommitType>()
    }

    gui.setTokens(tokens)
    project.messageBus
      .syncPublisher(DefaultTokensFileChangeListener.TOPIC)
      .fileChanged(project, tokens)
  }

  private fun refreshTokens(customFilePath: String?): Map<String, JsonCommitType> {
    return if (customFilePath != null) {
      CCDefaultTokensService.refreshTokens(customFilePath)
    } else {
      CCDefaultTokensService.DEFAULT_TOKENS
    }
  }

  override fun reset() {
    gui.completionType = config.completionType
    gui.customFilePath = config.customFilePath
  }

  override fun isModified() =
    gui.isValid
    && (gui.completionType != config.completionType
        || gui.customFilePath != config.customFilePath)

  override fun createComponent(): JComponent? {
    gui.completionType = config.completionType
    gui.customFilePath = config.customFilePath
    gui.setTokens(defaults.getDefaults())
    return gui.rootPanel
  }

  override fun disposeUIResources() {
    Disposer.dispose(disposable);
  }
}
