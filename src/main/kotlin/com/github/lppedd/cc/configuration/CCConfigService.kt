package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.DEFAULT_PROVIDER_ID
import com.github.lppedd.cc.DEFAULT_VCS_PROVIDER_ID
import com.github.lppedd.cc.STORAGE_FILE
import com.github.lppedd.cc.api.*
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XMap
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Edoardo Luppi
 */
@State(
  name = "general",
  storages = [Storage(STORAGE_FILE)]
)
internal class CCConfigService : PersistentStateComponent<CCConfigService> {
  companion object {
    fun getInstance(project: Project): CCConfigService =
      ServiceManager.getService(project, CCConfigService::class.java)
  }

  var completionType: CompletionType = CompletionType.POPUP
  var customFilePath: String? = null
  var scopeReplaceChar: String = "-"

  @XMap(
    propertyElementName = "commitTypes",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var typeProvidersMap: MutableMap<String, Int> = ConcurrentHashMap<String, Int>()

  @XMap(
    propertyElementName = "commitScopes",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var scopeProvidersMap: MutableMap<String, Int> = ConcurrentHashMap<String, Int>()

  @XMap(
    propertyElementName = "commitSubjects",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var subjectProvidersMap: MutableMap<String, Int> = ConcurrentHashMap<String, Int>()

  @XMap(
    propertyElementName = "commitBodies",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var bodyProvidersMap: MutableMap<String, Int> = ConcurrentHashMap<String, Int>()

  @XMap(
    propertyElementName = "commitFooterTypes",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var footerTypeProvidersMap: MutableMap<String, Int> = ConcurrentHashMap<String, Int>()

  @XMap(
    propertyElementName = "commitFooterValues",
    keyAttributeName = "providerId",
    valueAttributeName = "order"
  )
  private var footerValueProvidersMap: MutableMap<String, Int> = ConcurrentHashMap<String, Int>()

  init {
    noStateLoaded()
  }

  fun getProviderOrder(provider: CommitTypeProvider) =
    typeProvidersMap.computeIfAbsent(provider.getId()) { typeProvidersMap.size }

  fun getProviderOrder(provider: CommitScopeProvider) =
    scopeProvidersMap.computeIfAbsent(provider.getId()) { scopeProvidersMap.size }

  fun getProviderOrder(provider: CommitSubjectProvider) =
    subjectProvidersMap.computeIfAbsent(provider.getId()) { subjectProvidersMap.size }

  fun getProviderOrder(provider: CommitBodyProvider) =
    bodyProvidersMap.computeIfAbsent(provider.getId()) { bodyProvidersMap.size }

  fun getProviderOrder(provider: CommitFooterTypeProvider) =
    footerTypeProvidersMap.computeIfAbsent(provider.getId()) { footerTypeProvidersMap.size }

  fun getProviderOrder(provider: CommitFooterValueProvider) =
    footerValueProvidersMap.computeIfAbsent(provider.getId()) { footerValueProvidersMap.size }

  fun setTypeProvidersOrder(typeProvidersMap: Map<String, Int>) {
    this.typeProvidersMap = ConcurrentHashMap(typeProvidersMap)
  }

  fun setScopeProvidersOrder(scopeProvidersMap: Map<String, Int>) {
    this.scopeProvidersMap = ConcurrentHashMap(scopeProvidersMap)
  }

  fun setSubjectProvidersOrder(subjectProvidersMap: Map<String, Int>) {
    this.subjectProvidersMap = ConcurrentHashMap(subjectProvidersMap)
  }

  override fun getState() =
    this

  override fun loadState(state: CCConfigService) {
    XmlSerializerUtil.copyBean(state, this)
    noStateLoaded()
  }

  override fun noStateLoaded() {
    typeProvidersMap.putIfAbsent(DEFAULT_PROVIDER_ID, 0)
    scopeProvidersMap.putIfAbsent(DEFAULT_PROVIDER_ID, 0)
    subjectProvidersMap.putIfAbsent(DEFAULT_VCS_PROVIDER_ID, 0)
    footerTypeProvidersMap.putIfAbsent(DEFAULT_PROVIDER_ID, 0)
    footerValueProvidersMap.putIfAbsent(DEFAULT_PROVIDER_ID, 0)
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
