package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.*
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XMap
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Edoardo Luppi
 */
@State(
  name = "general",
  storages = [Storage(CC.Settings.File)]
)
internal class CCConfigService : PersistentStateComponent<CCConfigService> {
  var completionType: CompletionType = CompletionType.POPUP
  var providerFilterType: ProviderFilterType = ProviderFilterType.HIDE_SELECTED
  var customFilePath: String? = null
  var customCoAuthorsFilePath: String? = null
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

  fun setBodyProvidersOrder(bodyProvidersMap: Map<String, Int>) {
    this.bodyProvidersMap = ConcurrentHashMap(bodyProvidersMap)
  }

  fun setFooterTypeProvidersOrder(footerTypeProvidersMap: Map<String, Int>) {
    this.footerTypeProvidersMap = ConcurrentHashMap(footerTypeProvidersMap)
  }

  fun setFooterValueProvidersOrder(footerValueProvidersMap: Map<String, Int>) {
    this.footerValueProvidersMap = ConcurrentHashMap(footerValueProvidersMap)
  }

  override fun getState() =
    this

  override fun loadState(state: CCConfigService) {
    XmlSerializerUtil.copyBean(state, this)
    noStateLoaded()
  }

  override fun noStateLoaded() {
    typeProvidersMap.putIfAbsent(DefaultCommitTokenProvider.ID, 0)
    scopeProvidersMap.putIfAbsent(DefaultCommitTokenProvider.ID, 0)
    subjectProvidersMap.putIfAbsent(RecentCommitTokenProvider.ID, 0)
    footerTypeProvidersMap.putIfAbsent(DefaultCommitTokenProvider.ID, 0)
    footerValueProvidersMap.putIfAbsent(DefaultCommitTokenProvider.ID, 0)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CCConfigService) return false
    return Objects.equals(completionType, other.completionType) &&
           Objects.equals(customFilePath, other.customFilePath) &&
           Objects.equals(customCoAuthorsFilePath, other.customCoAuthorsFilePath) &&
           Objects.equals(typeProvidersMap, other.typeProvidersMap) &&
           Objects.equals(scopeProvidersMap, other.scopeProvidersMap) &&
           Objects.equals(subjectProvidersMap, other.subjectProvidersMap) &&
           Objects.equals(bodyProvidersMap, other.bodyProvidersMap) &&
           Objects.equals(footerTypeProvidersMap, other.footerTypeProvidersMap) &&
           Objects.equals(footerValueProvidersMap, other.footerValueProvidersMap)
  }

  override fun hashCode() =
    Objects.hash(
      completionType,
      customFilePath,
      customCoAuthorsFilePath,
      typeProvidersMap,
      scopeProvidersMap,
      subjectProvidersMap,
      bodyProvidersMap,
      footerTypeProvidersMap,
      footerValueProvidersMap,
    )

  enum class CompletionType {
    TEMPLATE,
    POPUP
  }

  enum class ProviderFilterType {
    KEEP_SELECTED,
    HIDE_SELECTED
  }
}
