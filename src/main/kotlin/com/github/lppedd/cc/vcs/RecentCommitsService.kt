package com.github.lppedd.cc.vcs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal interface RecentCommitsService {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): RecentCommitsService = project.service()
  }

  /**
   * Returns recently used commit types.
   */
  fun getRecentTypes(): Collection<String>

  /**
   * Returns recently used commit scopes.
   */
  fun getRecentScopes(): Collection<String>

  /**
   * Returns recently used commit subjects.
   */
  fun getRecentSubjects(): Collection<String>

  /**
   * Returns recently used commit footer values.
   */
  fun getRecentFooterValues(): Collection<String>

  /**
   * Returns the local (non-VCS) recently used commit messages.
   */
  fun getLocalMessageHistory(): Collection<String>

  /**
   * Clears the local (non-VCS) recently used commit messages.
   */
  fun clearLocalMessageHistory()
}
