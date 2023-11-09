package com.github.lppedd.cc.vcs.commitbuilder

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XMap
import java.util.*

/**
 * @author Edoardo Luppi
 */
@State(
    name = "com.github.lppedd.cc.CommitBuilderDialog",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)],
)
@Suppress("LightServiceMigrationCode")
internal class CommitBuilderService : PersistentStateComponent<CommitBuilderService> {
  @JvmField @Tag var shouldRemember: Boolean = true
  @JvmField @Tag var type: String = ""
  @JvmField @Tag var scope: String = ""
  @JvmField @Tag var subject: String = ""
  @JvmField @Tag var body: String = ""
  @JvmField @Tag var isBreakingChange: Boolean = false

  @XMap(
      propertyElementName = "footers",
      keyAttributeName = "type",
      valueAttributeName = "value",
  )
  private val footers: MutableMap<String, String> = mutableMapOf()

  fun clear() {
    type = ""
    scope = ""
    subject = ""
    body = ""
    isBreakingChange = false
    footers.clear()
  }

  fun getFooters(): Set<CommitFooter> =
    footers.map { CommitFooter(it.key, it.value) }.toSet()

  @Suppress("SameParameterValue")
  fun getFooter(footerType: String): CommitFooter? {
    val footerValue = footers[footerType] ?: return null
    return CommitFooter(footerType, footerValue)
  }

  fun addFooter(commitFooter: CommitFooter) {
    footers[commitFooter.type] = commitFooter.value
  }

  override fun getState(): CommitBuilderService =
    this

  override fun loadState(state: CommitBuilderService) {
    XmlSerializerUtil.copyBean(state, this)
  }

  override fun equals(other: Any?): Boolean {
    if (other !is CommitBuilderService) {
      return false
    }

    return type == other.type
           && scope == other.scope
           && subject == other.subject
           && body == other.body
           && isBreakingChange == other.isBreakingChange
           && footers == other.footers
           && shouldRemember == other.shouldRemember
  }

  override fun hashCode(): Int =
    Objects.hash(
        type,
        scope,
        subject,
        body,
        isBreakingChange,
        footers,
        shouldRemember,
    )

  data class CommitFooter(val type: String, val value: String) {
    override fun equals(other: Any?): Boolean =
      if (other is CommitFooter) {
        other.type.equals(type, true)
      } else {
        false
      }

    override fun hashCode(): Int =
      type.lowercase(Locale.getDefault()).hashCode()
  }
}
