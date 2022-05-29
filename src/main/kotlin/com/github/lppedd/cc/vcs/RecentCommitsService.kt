package com.github.lppedd.cc.vcs

/**
 * @author Edoardo Luppi
 */
interface RecentCommitsService {
  /** Returns recently used commit types. */
  fun getRecentTypes(): Collection<String>

  /** Returns recently used commit scopes. */
  fun getRecentScopes(): Collection<String>

  /** Returns recently used commit subjects. */
  fun getRecentSubjects(): Collection<String>

  /** Returns recently used commit footer values. */
  fun getRecentFooterValues(): Collection<String>

  /** Returns the local (non-VCS) recently used commit messages. */
  fun getLocalMessageHistory(): Collection<String>

  /** Clears the local (non-VCS) recently used commit messages. */
  fun clearLocalMessageHistory()
}
