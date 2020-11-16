package com.github.lppedd.cc.vcs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.VcsCommitMetadata
import com.intellij.vcs.log.data.VcsLogMultiRepoJoiner
import com.intellij.vcs.log.impl.VcsLogManager
import org.jetbrains.annotations.ApiStatus.*

/**
 * @author Edoardo Luppi
 */
@Internal
internal class CCVcsHandler(private val project: Project) {
  private val projectVcsManager = ProjectLevelVcsManager.getInstance(project)
  private val vcsLogMultiRepoJoiner = VcsLogMultiRepoJoiner<Hash, VcsCommitMetadata>()

  /**
   * Returns commits ordered by commit timestamp (latest first).
   */
  fun getOrderedCommits(limit: Int): Collection<VcsCommitMetadata> =
    getCommits(limit, VcsCommitMetadata::getCommitTime)

  private fun <T : Comparable<T>> getCommits(
      limit: Int,
      sortBy: (VcsCommitMetadata) -> T,
  ): Collection<VcsCommitMetadata> {
    val activeVcsRoots = projectVcsManager.allVcsRoots.toList()
    return VcsLogManager.findLogProviders(activeVcsRoots, project)
      .asSequence()
      .map { (root, logProvider) -> logProvider.readFirstBlock(root) { limit } }
      .map { it.commits }
      .toList()
      .let(vcsLogMultiRepoJoiner::join)
      .sortedByDescending(sortBy)
  }
}
