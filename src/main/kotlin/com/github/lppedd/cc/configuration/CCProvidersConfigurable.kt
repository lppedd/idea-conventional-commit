package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.CommitTypeProvider
import com.intellij.openapi.options.Configurable.NoScroll
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CCProvidersConfigurable(project: Project) : SearchableConfigurable, NoScroll {
  companion object {
    private val TYPE_EP = CommitTypeProvider.EP_NAME
    private val SCOPE_EP = CommitScopeProvider.EP_NAME
    private val SUBJECT_EP = CommitSubjectProvider.EP_NAME
  }

  private val gui = CCProvidersConfigurableGui()
  private val config = CCConfigService.getInstance(project)

  init {
    gui.setProviders(
      TYPE_EP.getExtensions(project).sortedBy(config::getProviderOrder),
      SCOPE_EP.getExtensions(project).sortedBy(config::getProviderOrder),
      SUBJECT_EP.getExtensions(project).sortedBy(config::getProviderOrder)
    )
  }

  override fun getId() = "preferences.${CCConstants.APP_NAME}.providers"
  override fun getDisplayName() = CCBundle["cc.config.providers"]

  override fun apply() {
    config.setTypeProvidersOrder(
      gui.typeProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    config.setScopeProvidersOrder(
      gui.scopeProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    config.setSubjectProvidersOrder(
      gui.subjectProviders
        .mapIndexed { index, provider -> provider.getId() to index }
        .toMap()
    )

    gui.setProviders(
      gui.typeProviders,
      gui.scopeProviders,
      gui.subjectProviders
    )
  }

  override fun reset() {
    gui.reset()
  }

  override fun isModified() = gui.isModified
  override fun createComponent() = gui.rootPanel
}
