package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
private class CCMainConfigurable(project: Project) : SearchableConfigurable {
  private val disposable = Disposer.newDisposable()
  private val gui = CCMainConfigurableGui(project, disposable)
  private val defaultsService = CCDefaultTokensService.getInstance(project)
  private val configService = CCConfigService.getInstance(project)

  override fun getId() = "preferences.${CC.AppName}"
  override fun getDisplayName() = CCBundle["cc.plugin.name"]

  override fun apply() {
    configService.completionType = gui.completionType
    configService.customCoAuthorsFilePath = gui.customCoAuthorsFilePath
    configService.customFilePath = gui.customTokensFilePath

    val tokens = try {
      defaultsService.getDefaultsFromCustomFile(configService.customFilePath)
    } catch (e: Exception) {
      gui.revalidate()
      return
    }

    gui.setTokens(tokens.types)
  }

  override fun reset() {
    gui.completionType = configService.completionType
    gui.customCoAuthorsFilePath = configService.customCoAuthorsFilePath
    gui.customTokensFilePath = configService.customFilePath
  }

  override fun isModified() =
    gui.isValid
    && (gui.completionType != configService.completionType ||
        gui.customCoAuthorsFilePath != configService.customCoAuthorsFilePath ||
        gui.customTokensFilePath != configService.customFilePath)

  override fun createComponent(): JComponent {
    gui.completionType = configService.completionType
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

  override fun disposeUIResources() {
    Disposer.dispose(disposable)
  }
}
