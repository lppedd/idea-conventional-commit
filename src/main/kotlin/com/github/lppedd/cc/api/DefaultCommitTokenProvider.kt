package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.api.CommitScopeProvider.CommitScope
import com.github.lppedd.cc.api.CommitSubjectProvider.CommitSubject
import com.github.lppedd.cc.api.CommitTypeProvider.CommitType
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration

/**
 * @author Edoardo Luppi
 */
internal class DefaultCommitTokenProvider(private val project: Project)
  : CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider {
  companion object {
    private val TYPES = listOf(
      CommitType("refactor", CCBundle["commit.type.refactor"]),
      CommitType("fix", CCBundle["commit.type.fix"]),
      CommitType("feat", CCBundle["commit.type.feat"]),
      CommitType("perf", CCBundle["commit.type.perf"]),
      CommitType("test", CCBundle["commit.type.test"]),
      CommitType("style", CCBundle["commit.type.style"]),
      CommitType("build", CCBundle["commit.type.build"]),
      CommitType("docs", CCBundle["commit.type.docs"]),
      CommitType("ci", CCBundle["commit.type.ci"])
    )
  }

  private val defaultsService = ServiceManager.getService(
    project,
    CCDefaultTokensService::class.java
  )

  override fun getId() = CCConstants.DEFAULT_PROVIDER_ID
  override fun getPresentationName() = "Default"
  override fun getPresentationIcon() = CCIcons.DEFAULT_PRESENTATION

  override fun getCommitTypes(prefix: String?) =
    defaultsService.getDefaults().map { CommitType(it.key, it.value.description) }

  override fun getCommitScopes(commitType: String?): List<CommitScope> {
    return when (commitType) {
      null -> emptyList()
      else -> {
        val scopes = defaultsService.getDefaults()[commitType]?.scopes
        scopes?.map { CommitScope(it.key, it.value.description) } ?: emptyList()
      }
    }
  }

  override fun getCommitSubjects(commitType: String?, commitScope: String?): List<CommitSubject> =
    getRecentVcsMessages(project)

  private fun getRecentVcsMessages(project: Project): List<CommitSubject> {
    val recentMessages = VcsConfiguration.getInstance(project).recentMessages.reversed()
    return recentMessages
      .take(20)
      .map { m -> m.replaceFirst("(^(${TYPES.joinToString("|") { it.text }})).*:".toRegex(), "") }
      .map { m -> m.trim { it <= ' ' } }
      .map { m -> CommitSubject(m) }
  }

  internal class JsonCommitType(
    var description: String? = null,
    var scopes: Map<String, JsonCommitScope>? = null
  )

  internal class JsonCommitScope(
    var description: String? = null
  )
}
