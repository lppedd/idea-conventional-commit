package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitTokenProviderService
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CCProvidersConfigurable(project: Project) : SearchableConfigurable {
  private val configService = project.service<CCConfigService>()
  private val providerService = project.service<CommitTokenProviderService>()
  private lateinit var gui: CCProvidersConfigurableGui

  override fun getId(): String =
    "preferences.${CC.AppName}.providers"

  override fun getDisplayName(): String =
    CCBundle["cc.config.providers"]

  override fun createComponent(): JPanel {
    gui = CCProvidersConfigurableGui()
    gui.setProviders(
      providerService.getTypeProviders(),
      providerService.getScopeProviders(),
      providerService.getSubjectProviders(),
      providerService.getBodyProviders(),
      providerService.getFooterTypeProviders(),
      providerService.getFooterValueProviders(),
    )

    return gui.rootPanel
  }

  override fun isModified(): Boolean =
    gui.isModified

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
}
