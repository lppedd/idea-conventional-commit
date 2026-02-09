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
import com.intellij.vcs.log.data.VcsLogMultiRepoJoiner
import com.intellij.vcs.log.graph.PermanentGraph
import com.intellij.vcs.log.impl.VcsLogManager
import com.intellij.vcs.log.impl.VcsProjectLog
import com.intellij.vcs.log.visible.filters.VcsLogFilterObject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.notExists

/**
 * @author Edoardo Luppi
 */
internal class InternalVcsService(private val project: Project) : VcsService {
  private data class VcsSnapshot(
    @JvmField val currentUsers: Set<VcsUser>,
    @JvmField val commits: List<VcsCommitMetadata>,
  )

  private companion object {
    private val logger = logger<InternalVcsService>()
  }

  private val refreshListeners = ConcurrentHashMap.newKeySet<VcsService.VcsListener>()
  private val vcsLogRefresher = MyVcsLogRefresher()
  private val subscribedVcsLogProviders = Collections.newSetFromMap<VcsLogProvider>(IdentityHashMap())

  @Volatile
  private var vcsSnapshot = VcsSnapshot(
    currentUsers = emptySet(),
    commits = emptyList(),
  )

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
      refreshVcsSnapshot()
      refreshListeners.forEach(VcsService.VcsListener::onRefresh)
    }
  }

  override fun getCurrentUsers(): Collection<VcsUser> =
    vcsSnapshot.currentUsers

  override fun getOrderedTopCommits(): Collection<VcsCommitMetadata> =
    vcsSnapshot.commits

  override fun addListener(listener: VcsService.VcsListener) {
    refreshListeners.add(listener)
  }

  private fun refreshVcsSnapshot() {
    val vcsLogProviders = getVcsLogProviders()
    val users = fetchCurrentUsers(vcsLogProviders)
    val commits = fetchCommits(vcsLogProviders, sortBy = VcsCommitMetadata::getCommitTime)
    vcsSnapshot = VcsSnapshot(currentUsers = users, commits = commits)
  }

  private fun fetchCurrentUsers(vcsLogProviders: Map<VirtualFile, VcsLogProvider>): Set<VcsUser> =
    vcsLogProviders
      .mapNotNull { (root, vcsLogProvider) ->
        safeLogAccess("getCurrentUser") {
          vcsLogProvider.getCurrentUser(root)
        }
      }.toSet()

  @Suppress("UnstableApiUsage")
  private fun <T : Comparable<T>> fetchCommits(
    vcsLogProviders: Map<VirtualFile, VcsLogProvider>,
    sortBy: (VcsCommitMetadata) -> T,
  ): List<VcsCommitMetadata> =
    vcsLogProviders
      .map { (root, vcsLogProvider) -> fetchCommitsFromLogProvider(root, vcsLogProvider) }
      .toList()
      .let { VcsLogMultiRepoJoiner<Hash, VcsCommitMetadata>().join(it) }
      .sortedByDescending(sortBy)

  private fun fetchCommitsFromLogProvider(root: VirtualFile, vscLogProvider: VcsLogProvider): List<VcsCommitMetadata> {
    val localPath = root.fileSystem.getNioPath(root)

    // If the repository root is represented by a locally stored file,
    // we check if that file still exists
    if (localPath != null && localPath.notExists()) {
      return emptyList()
    }

    val vcsRepositoryManager = VcsRepositoryManager.getInstance(project)
    val repository = vcsRepositoryManager.getRepositoryForRoot(root)

    // If the repository is fresh, it means it doesn't have commits yet, and so no branches.
    // See https://youtrack.jetbrains.com/issue/IDEA-255522
    if (repository == null || repository.isFresh) {
      return emptyList()
    }

    val currentBranch = safeLogAccess("getCurrentBranch") {
      vscLogProvider.getCurrentBranch(root)
    }

    if (currentBranch == null) {
      return emptyList()
    }

    val branchFilter = VcsLogFilterObject.fromBranch(currentBranch)
    val filterCollection = VcsLogFilterObject.collection(branchFilter)

    // Apparently, IDEA's VCS log might contain refs to commits that don't exist anymore.
    // It might be simply a matter of refreshing the log for the user, but here it's
    // slightly more complex - to the point the most reasonable choice is to catch
    // the exception and return an empty result.
    val matchingCommits = safeLogAccess("getCommitsMatchingFilter") {
      vscLogProvider.getCommitsMatchingFilter(root, filterCollection, PermanentGraph.Options.Default, 100)
    }

    if (matchingCommits.isNullOrEmpty()) {
      return emptyList()
    }

    val commitsMetadata = ArrayList<VcsCommitMetadata>(100)
    val hashes = matchingCommits.map { it.id.asString() }
    safeLogAccess("readMetadata") {
      vscLogProvider.readMetadata(root, hashes, commitsMetadata::add)
    }

    return commitsMetadata
  }

  private fun getVcsLogProviders(): Map<VirtualFile, VcsLogProvider> {
    val projectVcsManager = ProjectLevelVcsManager.getInstance(project)
    val activeVcsRoots = projectVcsManager.getAllVcsRoots().toList()
    return VcsLogManager.findLogProviders(activeVcsRoots, project)
  }

  private fun <T> safeLogAccess(function: String, block: () -> T): T? {
    try {
      return block()
    } catch (e: VcsException) {
      logger.debug("Error calling VcsLogProvider.$function", e)
    } catch (e: IllegalStateException) {
      if (e is CancellationException) {
        throw e
      }

      logger.debug("Error calling VcsLogProvider.$function - see IJPL-148354", e)
    }

    return null
  }

  /*
   * Methods from here onwards are unused, but we keep them for future reference.
   * They might come handy.
   */

  @Suppress("unused")
  private fun getPossiblyCachedCommitsData(root: VirtualFile, commits: List<TimedVcsCommit>): Collection<VcsCommitMetadata> {
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
      refreshVcsSnapshot()
      refreshListeners.forEach(VcsService.VcsListener::onRefresh)
    }
  }
}
