package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.CCRegistry
import com.github.lppedd.cc.parser.ParseResult
import com.github.lppedd.cc.parser.parseConventionalCommit
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.vcs.log.VcsCommitMetadata

/**
 * @author Edoardo Luppi
 */
internal class InternalRecentCommitsService(private val project: Project) : RecentCommitsService {
  private data class RecentSnapshot(
    @JvmField val types: Set<String>,
    @JvmField val scopes: Set<String>,
    @JvmField val subjects: Set<String>,
    @JvmField val footerValues: Set<String>,
  )

  private companion object {
    private const val MAX_SAVED_MESSAGES = 25
    private const val MAX_ELEMENTS = 4
  }

  private val vcsService = VcsService.getInstance(project)

  @Volatile
  private var recentSnapshot: RecentSnapshot? = null
  private val recentSnapshotLock = Any()

  init {
    vcsService.addListener(object : VcsService.VcsListener {
      override fun onRefresh() {
        recentSnapshot = null
      }
    })
  }

  override fun getRecentTypes(): Collection<String> =
    getRecentSnapshot().types

  override fun getRecentScopes(): Collection<String> =
    getRecentSnapshot().scopes

  override fun getRecentSubjects(): Collection<String> =
    getRecentSnapshot().subjects

  override fun getRecentFooterValues(): Collection<String> =
    getRecentSnapshot().footerValues

  override fun getLocalMessageHistory(): Collection<String> {
    val vcsConfiguration = VcsConfiguration.getInstance(project)
    return vcsConfiguration.recentMessages
  }

  override fun clearLocalMessageHistory() {
    val vcsConfiguration = VcsConfiguration.getInstance(project)
    vcsConfiguration.myLastCommitMessages.clear()
    recentSnapshot = null
  }

  private fun getRecentSnapshot(): RecentSnapshot {
    var snapshot = recentSnapshot

    if (snapshot != null) {
      return snapshot
    }

    return synchronized(recentSnapshotLock) {
      snapshot = recentSnapshot

      if (snapshot != null) {
        return@synchronized snapshot
      }

      snapshot = updateRecentSnapshot()
      recentSnapshot = snapshot
      return@synchronized snapshot
    }
  }

  private fun updateRecentSnapshot(): RecentSnapshot {
    val types = LinkedHashSet<String>()
    val scopes = LinkedHashSet<String>()
    val subjects = LinkedHashSet<String>()
    val footerValues = LinkedHashSet<String>()

    fun parseMessage(message: String) {
      when (val result = parseConventionalCommit(message)) {
        is ParseResult.Success -> {
          val parsed = result.message
          types.addUpTo(parsed.type)

          if (!parsed.scope.isNullOrEmpty()) {
            scopes.addUpTo(parsed.scope)
          }

          if (parsed.subject.isNotEmpty()) {
            subjects.addUpTo(parsed.subject)
          }

          for ((_, value) in parsed.footers) {
            if (value.isNotEmpty()) {
              footerValues.addUpTo(value)
            }
          }
        }
        is ParseResult.Error -> {
          /* Skip message */
        }
      }
    }

    getOrderedSavedCommitMessages().forEach(::parseMessage)
    getOrderedVcsCommitMessages().forEach(::parseMessage)

    return RecentSnapshot(
      types = types,
      scopes = scopes,
      subjects = subjects,
      footerValues = footerValues,
    )
  }

  private fun getOrderedVcsCommitMessages(): Collection<String> {
    if (!CCRegistry.isVcsSupportEnabled()) {
      return emptyList()
    }

    val currentUsers = vcsService.getCurrentUsers()
    return vcsService.getOrderedTopCommits()
      .asSequence()
      .filter { currentUsers.contains(it.author) }
      .map(VcsCommitMetadata::getFullMessage)
      .toList()
  }

  private fun getOrderedSavedCommitMessages(): Collection<String> {
    val vcsConfiguration = VcsConfiguration.getInstance(project)
    return vcsConfiguration.recentMessages.asReversed().take(MAX_SAVED_MESSAGES)
  }

  private fun MutableSet<String>.addUpTo(value: String) {
    if (size < MAX_ELEMENTS) {
      add(value)
    }
  }
}
