package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.completion.Priority
import com.github.lppedd.cc.configuration.CCConfigService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TypeProviderWrapper(
    project: Project,
    private val provider: CommitTypeProvider,
) : ProviderWrapper {
  private val config = project.service<CCConfigService>()

  override fun getId(): String =
    provider.getId()

  override fun getPresentation(): ProviderPresentation =
    provider.getPresentation()

  override fun getPriority() =
    Priority(config.getProviderOrder(provider))
}
