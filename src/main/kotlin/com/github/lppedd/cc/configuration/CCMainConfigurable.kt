package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCConstants
import com.intellij.openapi.options.Configurable.NoScroll
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
internal class CCMainConfigurable(project: Project) : SearchableConfigurable, NoScroll {
  private val disposable = Disposer.newDisposable()
  private val gui = CCMainConfigurableGui(project, disposable)
  private val defaultsService = CCDefaultTokensService.getInstance(project)
  private val configService = CCConfigService.getInstance(project)

  override fun getId() = "preferences.${CCConstants.APP_NAME}"
  override fun getDisplayName() = CCBundle["cc.plugin.name"]

  override fun apply() {
    configService.completionType = gui.completionType
    configService.customFilePath = gui.customFilePath

    val tokens = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      gui.revalidate()
      return
    }

    gui.setTokens(tokens)
  }

  override fun reset() {
    gui.completionType = configService.completionType
    gui.customFilePath = configService.customFilePath
  }

  override fun isModified() =
    gui.isValid
    && (gui.completionType != configService.completionType
        || gui.customFilePath != configService.customFilePath)

  override fun createComponent(): JComponent? {
    gui.completionType = configService.completionType
    gui.customFilePath = configService.customFilePath

    val tokens = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      defaultsService.getBuiltInDefaults()
    }

    gui.setTokens(tokens)
    return gui.rootPanel
  }

  override fun disposeUIResources() {
    Disposer.dispose(disposable)
  }
}
