package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.TestOnly

/**
 * @author Edoardo Luppi
 */
internal class InternalCommitTokenProviderService(private val project: Project) : CommitTokenProviderService {
  private companion object {
    private val typeEpName: ProjectExtensionPointName<CommitTypeProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitTypeProvider")

    private val scopeEpName: ProjectExtensionPointName<CommitScopeProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitScopeProvider")

    private val subjectEpName: ProjectExtensionPointName<CommitSubjectProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitSubjectProvider")

    private val bodyEpName: ProjectExtensionPointName<CommitBodyProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitBodyProvider")

    private val footerTypeEpName: ProjectExtensionPointName<CommitFooterTypeProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitFooterTypeProvider")

    private val footerValueEpName: ProjectExtensionPointName<CommitFooterValueProvider> =
      ProjectExtensionPointName("com.github.lppedd.idea-conventional-commit.commitFooterValueProvider")
  }

  override fun getTypeProviders(): List<CommitTypeProvider> {
    val configService = CCConfigService.getInstance(project)
    val providers = typeEpName.getExtensions(project)
    return providers.sortedBy(configService::getProviderOrder)
  }

  override fun getScopeProviders(): List<CommitScopeProvider> {
    val configService = CCConfigService.getInstance(project)
    val providers = scopeEpName.getExtensions(project)
    return providers.sortedBy(configService::getProviderOrder)
  }

  override fun getSubjectProviders(): List<CommitSubjectProvider> {
    val configService = CCConfigService.getInstance(project)
    val providers = subjectEpName.getExtensions(project)
    return providers.sortedBy(configService::getProviderOrder)
  }

  override fun getBodyProviders(): List<CommitBodyProvider> {
    val configService = CCConfigService.getInstance(project)
    val providers = bodyEpName.getExtensions(project)
    return providers.sortedBy(configService::getProviderOrder)
  }

  override fun getFooterTypeProviders(): List<CommitFooterTypeProvider> {
    val configService = CCConfigService.getInstance(project)
    val providers = footerTypeEpName.getExtensions(project)
    return providers.sortedBy(configService::getProviderOrder)
  }

  override fun getFooterValueProviders(): List<CommitFooterValueProvider> {
    val configService = CCConfigService.getInstance(project)
    val providers = footerValueEpName.getExtensions(project)
    return providers.sortedBy(configService::getProviderOrder)
  }

  @TestOnly
  override fun registerTypeProvider(typeProvider: CommitTypeProvider, disposable: Disposable) =
    typeEpName.getPoint(project).registerExtension(typeProvider, disposable)

  @TestOnly
  override fun registerScopeProvider(scopeProvider: CommitScopeProvider, disposable: Disposable) =
    scopeEpName.getPoint(project).registerExtension(scopeProvider, disposable)

  @TestOnly
  override fun registerSubjectProvider(subjectProvider: CommitSubjectProvider, disposable: Disposable) =
    subjectEpName.getPoint(project).registerExtension(subjectProvider, disposable)

  @TestOnly
  override fun registerBodyProvider(bodyProvider: CommitBodyProvider, disposable: Disposable) =
    bodyEpName.getPoint(project).registerExtension(bodyProvider, disposable)

  @TestOnly
  override fun registerFooterTypeProvider(footerTypeProvider: CommitFooterTypeProvider, disposable: Disposable) =
    footerTypeEpName.getPoint(project).registerExtension(footerTypeProvider, disposable)

  @TestOnly
  override fun registerFooterValueProvider(footerValueProvider: CommitFooterValueProvider, disposable: Disposable) =
    footerValueEpName.getPoint(project).registerExtension(footerValueProvider, disposable)
}
