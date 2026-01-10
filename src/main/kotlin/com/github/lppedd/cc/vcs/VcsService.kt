package com.github.lppedd.cc.vcs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager.Companion.VCS_CONFIGURATION_CHANGED
import com.intellij.vcs.log.VcsCommitMetadata
import com.intellij.vcs.log.VcsUser

/**
 * @author Edoardo Luppi
 */
internal interface VcsService {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): VcsService = project.service()
  }

  /**
   * Called on every [VCS_CONFIGURATION_CHANGED].
   */
  fun refresh()

  /**
   * Returns the user data associated with the active VCS roots.
   */
  fun getCurrentUsers(): Collection<VcsUser>

  /**
   * Returns at most the top 100 commits for the currently checked-out branch,
   * ordered by commit timestamp (latest first).
   */
  fun getOrderedTopCommits(): Collection<VcsCommitMetadata>

  /**
   * Adds a listener to the list of listeners that will be notified
   * on VCS-related changes.
   */
  fun addListener(listener: VcsListener)
}
