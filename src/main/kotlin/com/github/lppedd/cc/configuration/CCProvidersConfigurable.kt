package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.*
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
private class CCProvidersConfigurable(private val project: Project) : SearchableConfigurable {
  private val configService = project.service<CCConfigService>()
  private lateinit var gui: CCProvidersConfigurableGui

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

    configService.setBodyProvidersOrder(
      gui.bodyProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    configService.setFooterTypeProvidersOrder(
      gui.footerTypeProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    configService.setFooterValueProvidersOrder(
      gui.footerValueProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    gui.setProviders(
      gui.typeProviders,
      gui.scopeProviders,
      gui.subjectProviders,
      gui.bodyProviders,
      gui.footerTypeProviders,
      gui.footerValueProviders,
    )
  }

  override fun reset() {
    gui.reset()
  }

  override fun isModified() = gui.isModified
  override fun createComponent(): JPanel {
    gui = CCProvidersConfigurableGui()
    gui.setProviders(
      TYPE_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      SCOPE_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      SUBJECT_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      BODY_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      FOOTER_TYPE_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
      FOOTER_VALUE_EP.getExtensions(project).sortedBy(configService::getProviderOrder),
    )

    return gui.rootPanel
  }
}
