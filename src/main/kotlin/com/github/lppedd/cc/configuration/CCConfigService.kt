package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.DEFAULT_PROVIDER_ID
import com.github.lppedd.cc.STORAGE_FILE
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.CommitTypeProvider
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XMap
import java.util.*

/**
 * @author Edoardo Luppi
 */
@State(
  name = "general",
  storages = [Storage(STORAGE_FILE)]
)
internal class CCConfigService : PersistentStateComponent<CCConfigService> {
  companion object {
    private val DEFAULT_ENTRY = Pair(DEFAULT_PROVIDER_ID, 0)

    fun getInstance(project: Project): CCConfigService =
      ServiceManager.getService(project, CCConfigService::class.java)
  }

  var completionType: CompletionType = CompletionType.POPUP
  var customFilePath: String? = null

  @XMap(
    propertyElementName = "commitTypes",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var typeProvidersMap: Map<String, Int> = hashMapOf(DEFAULT_ENTRY)

  @XMap(
    propertyElementName = "commitScopes",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var scopeProvidersMap: Map<String, Int> = hashMapOf(DEFAULT_ENTRY)

  @XMap(
    propertyElementName = "commitSubjects",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var subjectProvidersMap: Map<String, Int> = hashMapOf(DEFAULT_ENTRY)

  fun getProviderOrder(provider: CommitTypeProvider) = typeProvidersMap[provider.getId()] ?: 0
  fun getProviderOrder(provider: CommitScopeProvider) = scopeProvidersMap[provider.getId()] ?: 0
  fun getProviderOrder(provider: CommitSubjectProvider) = subjectProvidersMap[provider.getId()] ?: 0

  fun setTypeProvidersOrder(typeProvidersMap: Map<String, Int>) {
    this.typeProvidersMap = typeProvidersMap
  }

  fun setScopeProvidersOrder(scopeProvidersMap: Map<String, Int>) {
    this.scopeProvidersMap = scopeProvidersMap
  }

  fun setSubjectProvidersOrder(subjectProvidersMap: Map<String, Int>) {
    this.subjectProvidersMap = subjectProvidersMap
  }

  override fun getState() = this
  override fun loadState(state: CCConfigService) {
    XmlSerializerUtil.copyBean(state, this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CCConfigService) return false
    return Objects.equals(completionType, other.completionType) &&
           Objects.equals(customFilePath, other.customFilePath) &&
           Objects.equals(typeProvidersMap, other.typeProvidersMap) &&
           Objects.equals(scopeProvidersMap, other.scopeProvidersMap) &&
           Objects.equals(subjectProvidersMap, other.subjectProvidersMap)
  }

  override fun hashCode() =
    Objects.hash(
      completionType,
      customFilePath,
      typeProvidersMap,
      scopeProvidersMap,
      subjectProvidersMap
    )

  enum class CompletionType {
    TEMPLATE,
    POPUP
  }
}
