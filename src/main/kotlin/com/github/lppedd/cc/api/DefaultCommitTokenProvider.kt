package com.github.lppedd.cc.api

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitScopeProvider.CommitScope
import com.github.lppedd.cc.api.CommitSubjectProvider.CommitSubject
import com.github.lppedd.cc.api.CommitTypeProvider.CommitType
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration

/**
 * @author Edoardo Luppi
 */
internal class DefaultCommitTokenProvider(private val project: Project)
  : CommitTypeProvider,
    CommitScopeProvider,
    CommitSubjectProvider {
  private val defaultsService = CCDefaultTokensService.getInstance(project)

  override fun getId() = CCConstants.DEFAULT_PROVIDER_ID
  override fun getPresentation() =
    ProviderPresentation(
      "Default",
      CCIcons.DEFAULT_PRESENTATION
    )

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
    return VcsConfiguration.getInstance(project).recentMessages
      .asReversed()
      .asSequence()
      .take(20)
      .map(CCParser::parseText)
      .map(PCommitTokens::subject)
      .map(PCommitSubject::value)
      .map(String::trim)
      .filter(String::isNotEmpty)
      .map(::CommitSubject)
      .toList()
  }

  internal class JsonCommitType(
    var description: String? = null,
    var scopes: Map<String, JsonCommitScope>? = null
  )

  internal class JsonCommitScope(
    var description: String? = null
  )
}
