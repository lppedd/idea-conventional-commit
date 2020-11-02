package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.*
import com.intellij.openapi.options.Configurable.NoScroll
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CCProvidersConfigurable(project: Project) : SearchableConfigurable, NoScroll {
  private val gui = CCProvidersConfigurableGui()
  private val configService = CCConfigService.getInstance(project)

  init {
    gui.setProviders(
      TYPE_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      SCOPE_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      SUBJECT_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
    )
  }

  override fun getId() = "preferences.${CC.AppName}.providers"
  override fun getDisplayName() = CCBundle["cc.config.providers"]

  override fun apply() {
    configService.setTypeProvidersOrder(
      gui.typeProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    configService.setScopeProvidersOrder(
      gui.scopeProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    configService.setSubjectProvidersOrder(
      gui.subjectProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    gui.setProviders(
      gui.typeProviders,
      gui.scopeProviders,
      gui.subjectProviders,
    )
  }

  override fun reset() {
    gui.reset()
  }

  override fun isModified() = gui.isModified
  override fun createComponent() = gui.rootPanel
}
