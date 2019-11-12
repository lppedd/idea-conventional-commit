package com.github.lppedd.cc.extensions

import com.github.lppedd.cc.ConventionalCommitBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration

/**
 * @author Edoardo Luppi
 */
internal object DefaultConventionalCommitProvider : AbstractConventionalCommitProvider() {
  val TYPES = listOf(
    CommitType("refactor", ConventionalCommitBundle["commit.type.refactor"]),
    CommitType("fix", ConventionalCommitBundle["commit.type.fix"]),
    CommitType("feat", ConventionalCommitBundle["commit.type.feat"]),
    CommitType("perf", ConventionalCommitBundle["commit.type.perf"]),
    CommitType("test", ConventionalCommitBundle["commit.type.test"]),
    CommitType("style", ConventionalCommitBundle["commit.type.style"]),
    CommitType("build", ConventionalCommitBundle["commit.type.build"]),
    CommitType("docs", ConventionalCommitBundle["commit.type.docs"]),
    CommitType("ci", ConventionalCommitBundle["commit.type.ci"])
  )

  private val SCOPES = listOf(
    CommitScope("npm"),
    CommitScope("gulp"),
    CommitScope("broccoli")
  )

  override fun getCommitTypes(): List<CommitType> = TYPES
  override fun getCommitScopes(commitType: String?): List<CommitScope> =
    when (commitType) {
      "build" -> SCOPES
      else -> emptyList()
    }

  override fun getCommitSubjects(
    project: Project,
    commitType: String?,
    commitScope: String?
  ): List<CommitSubject> =
    getRecentVcsMessages(project, 20)

  @Suppress("SameParameterValue")
  private fun getRecentVcsMessages(project: Project, limit: Int): List<CommitSubject> {
    val recentMessages = VcsConfiguration.getInstance(project).recentMessages.reversed()
    return recentMessages
      .take(limit)
      .map { v -> v.replaceFirst("(^(${TYPES.joinToString("|") { it.name }})).*:".toRegex(), "") }
      .map { obj -> obj.trim { it <= ' ' } }
      .map { description -> CommitSubject(description) }
  }
}
