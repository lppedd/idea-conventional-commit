package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.invokeLaterOnEdtAndWait
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.log.*
import com.intellij.vcs.log.impl.VcsLogManager
import com.intellij.vcs.log.impl.VcsProjectLog
import com.intellij.vcs.log.visible.filters.VcsLogFilterObject
import java.util.*
import java.util.Collections.newSetFromMap
import kotlin.io.path.notExists

/**
 * @author Edoardo Luppi
 */
internal class InternalVcsService(private val project: Project) : VcsService {
  private companion object {
    private val logger = logger<InternalVcsService>()
  }

  private val refreshListeners = mutableSetOf<VcsListener>()
  private val vcsLogRefresher = MyVcsLogRefresher()
  private val projectVcsManager = ProjectLevelVcsManager.getInstance(project)
  private val vcsRepositoryManager = VcsRepositoryManager.getInstance(project)
  private val vcsLogMultiRepoJoiner = VcsLogMultiRepoJoiner<Hash, VcsCommitMetadata>()
  private val subscribedVcsLogProviders = newSetFromMap<VcsLogProvider>(IdentityHashMap(16))

  @Volatile
  private var cachedCurrentUser: Collection<VcsUser> = emptyList()

  @Volatile
  private var cachedCommits: Collection<VcsCommitMetadata> = emptyList()

  override fun refresh() {
    val vcsLogProviders = getVcsLogProviders()

    synchronized(subscribedVcsLogProviders) {
      // Remove unused VcsLogProvider(s)
      @Suppress("ConvertArgumentToSet")
      subscribedVcsLogProviders.retainAll(vcsLogProviders.values)

      // Unlike what VcsLogManager does, we subscribe only a single root directory
      // even if more than one is mapped to the same VcsLogProvider.
      // We do this to avoid multiple refreshes each time the VCS configuration changes
      for ((root, vcsLogProvider) in vcsLogProviders) {
        if (!subscribedVcsLogProviders.contains(vcsLogProvider)) {
          subscribedVcsLogProviders.add(vcsLogProvider)
          vcsLogProvider.subscribeToRootRefreshEvents(listOf(root), vcsLogRefresher)
        }
      }
    }

    if (vcsLogProviders.isNotEmpty()) {
      refreshCachedValues()
      refreshListeners.forEach(VcsListener::refreshed)
    }
  }

  override fun getCurrentUsers(): Collection<VcsUser> = cachedCurrentUser

  override fun getOrderedTopCommits(): Collection<VcsCommitMetadata> = cachedCommits

  override fun addListener(listener: VcsListener) {
    refreshListeners.add(listener)
  }

  private fun refreshCachedValues() {
    cachedCurrentUser = fetchCurrentUsers()
    cachedCommits = fetchCommits(sortBy = VcsCommitMetadata::getCommitTime)
  }

  private fun fetchCurrentUsers(): Set<VcsUser> =
    getVcsLogProviders().asSequence()
      .map { (root, vcsLogProvider) -> vcsLogProvider.getCurrentUser(root) }
      .filterNotNull()
      .toSet()

  private fun <T : Comparable<T>> fetchCommits(sortBy: (VcsCommitMetadata) -> T): List<VcsCommitMetadata> =
    getVcsLogProviders().asSequence()
      .map { (root, vcsLogProvider) -> fetchCommitsFromLogProvider(root, vcsLogProvider) }
      .toList()
      .let(vcsLogMultiRepoJoiner::join)
      .sortedByDescending(sortBy)

  private fun fetchCommitsFromLogProvider(
      root: VirtualFile,
      logProvider: VcsLogProvider,
  ): List<VcsCommitMetadata> {
    val localPath = root.fileSystem.getNioPath(root)

    // If the repository root is represented by a locally stored file,
    // we check if that file still exist
    if (localPath != null && localPath.notExists()) {
      return emptyList()
    }

    val repository = vcsRepositoryManager.getRepositoryForRoot(root)

    // If the repository is fresh it means it doesn't have commits yet, and so no branches.
    // See https://youtrack.jetbrains.com/issue/IDEA-255522
    if (repository == null || repository.isFresh) {
      return emptyList()
    }

    val currentBranch = logProvider.getCurrentBranch(root) ?: return emptyList()
    val branchFilter = VcsLogFilterObject.fromBranch(currentBranch)
    val filterCollection = VcsLogFilterObject.collection(branchFilter)

    // Apparently IDEA's VCS log might contain refs to commits that don't exist anymore.
    // It might be simply a matter of refreshing the log for the user, but here it's
    // slightly more complex - to the point the most reasonable choice is to catch
    // the exception and return an empty result.
    val matchingCommits = try {
      logProvider.getCommitsMatchingFilter(root, filterCollection, 100)
    } catch (e: VcsException) {
      logger.debug("Error retrieving commits via VcsLogProvider", e)
      return emptyList()
    }

    if (matchingCommits.isEmpty()) {
      return emptyList()
    }

    val commitsMetadata = ArrayList<VcsCommitMetadata>(100)
    val hashes = matchingCommits.map { it.id.asString() }
    logProvider.readMetadata(root, hashes, commitsMetadata::add)
    return commitsMetadata
  }

  private fun getVcsLogProviders(): Map<VirtualFile, VcsLogProvider> {
    val activeVcsRoots = projectVcsManager.allVcsRoots.toList()
    return VcsLogManager.findLogProviders(activeVcsRoots, project)
  }

  /*
   * Methods from here onwards are unused, but we keep them for future reference.
   * They might come handy.
   */

  @Suppress("unused")
  private fun getPossiblyCachedCommitsData(
      root: VirtualFile,
      commits: List<TimedVcsCommit>,
  ): Collection<VcsCommitMetadata> {
    val vcsLogData = VcsProjectLog.getInstance(project).dataManager!!
    val vcsLogStorage = vcsLogData.storage
    val matchingCommitsIndexes = commits.map { vcsLogStorage.getCommitIndex(it.id, root) }
    val progressIndicator = ProgressManager.getInstance().progressIndicator
    var commitsMetadata = emptyList<VcsCommitMetadata>()

    invokeLaterOnEdtAndWait {
      vcsLogData.miniDetailsGetter.loadCommitsData(
          matchingCommitsIndexes,
          { commitsMetadata = it },
          {},
          progressIndicator,
      )
    }

    return commitsMetadata
  }

  private inner class MyVcsLogRefresher : VcsLogRefresher {
    override fun refresh(root: VirtualFile) {
      refreshCachedValues()
      refreshListeners.forEach(VcsListener::refreshed)
    }
  }
}
