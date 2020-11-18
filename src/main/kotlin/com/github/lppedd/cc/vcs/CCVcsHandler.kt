package com.github.lppedd.cc.vcs

import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.invokeLaterOnEdtAndWait
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer
import com.intellij.util.EmptyConsumer
import com.intellij.vcs.log.*
import com.intellij.vcs.log.data.VcsLogMultiRepoJoiner
import com.intellij.vcs.log.impl.VcsLogContentUtil
import com.intellij.vcs.log.impl.VcsLogManager
import com.intellij.vcs.log.impl.VcsProjectLog
import com.intellij.vcs.log.visible.filters.VcsLogFilterObject
import org.jetbrains.annotations.ApiStatus.*
import java.util.*
import java.util.Collections.newSetFromMap
import kotlin.collections.ArrayList

/**
 * @author Edoardo Luppi
 */
@Internal
internal class CCVcsHandler(private val project: Project) : VcsLogRefresher {
  private val projectVcsManager = ProjectLevelVcsManager.getInstance(project)
  private val vcsLogMultiRepoJoiner = VcsLogMultiRepoJoiner<Hash, VcsCommitMetadata>()
  private val subscribedVcsLogProviders = newSetFromMap<VcsLogProvider>(IdentityHashMap(16))
  private lateinit var cachedCommits: Collection<VcsCommitMetadata>
  private lateinit var cachedCurrentUser: Collection<VcsUser>

  /**
   * Called on every [ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED].
   */
  fun reset() {
    val vcsLogProviders = getVcsLogProviders()

    synchronized(subscribedVcsLogProviders) {
      // Remove unused VcsLogProvider(s)
      subscribedVcsLogProviders.retainAll(vcsLogProviders.values)

      // Unlike what VcsLogManager does, we subscribe only a single root directory
      // even if more than one is mapped to the same VcsLogProvider.
      // We do this to avoid multiple refreshes each time the VCS configuration changes
      for ((root, vcsLogProvider) in vcsLogProviders) {
        if (subscribedVcsLogProviders.contains(vcsLogProvider).not()) {
          subscribedVcsLogProviders.add(vcsLogProvider)
          vcsLogProvider.subscribeToRootRefreshEvents(listOf(root), this)
        }
      }
    }

    if (vcsLogProviders.isNotEmpty()) {
      refreshCachedValues()
    }
  }

  /**
   * Returns the user-configured data associated with the active VCS roots.
   */
  @Synchronized
  fun getCurrentUser(): Collection<VcsUser> =
    cachedCurrentUser

  /**
   * Returns at most the top 100 commits for the currently checked-out branch,
   * ordered by commit timestamp (latest first).
   */
  @Synchronized
  fun getOrderedTopCommits(): Collection<VcsCommitMetadata> =
    cachedCommits

  override fun refresh(root: VirtualFile) {
    refreshCachedValues()
  }

  @Synchronized
  private fun refreshCachedValues() {
    cachedCurrentUser = fetchCurrentUser()
    cachedCommits = fetchCommits(sortBy = VcsCommitMetadata::getCommitTime)
  }

  private fun fetchCurrentUser(): Set<VcsUser> =
    getVcsLogProviders().asSequence()
      .map { (root, vcsLogProvider) -> vcsLogProvider.getCurrentUser(root) }
      .filterNotNull()
      .toSet()

  private fun <T : Comparable<T>> fetchCommits(sortBy: (VcsCommitMetadata) -> T): List<VcsCommitMetadata> =
    getVcsLogProviders().asSequence()
      .map { (root, vcsLogProvider) -> getCommits(root, vcsLogProvider) }
      .toList()
      .let(vcsLogMultiRepoJoiner::join)
      .sortedByDescending(sortBy)

  private fun getCommits(root: VirtualFile, logProvider: VcsLogProvider): List<VcsCommitMetadata> {
    val currentBranch = logProvider.getCurrentBranch(root) ?: return emptyList()
    val branchFilter = VcsLogFilterObject.fromBranch(currentBranch)
    val filterCollection = VcsLogFilterObject.collection(branchFilter)
    val matchingCommits = logProvider.getCommitsMatchingFilter(root, filterCollection, 100)

    // An explicit check is required to avoid a VcsException
    // that gets thrown when the current branch doesn't have any commits.
    // The root cause is VcsLogProvider#getCurrentBranch returns a branch name
    // even if the repository hasn't got any
    return if (matchingCommits.isNotEmpty()) {
      logProvider.readMetadataComp(root, matchingCommits.map { it.id.asString() })
    } else {
      emptyList()
    }
  }

  @Compatibility(minVersion = "203.4203.26")
  private fun VcsLogProvider.readMetadataComp(
      root: VirtualFile,
      commitsHashes: List<String>,
  ): List<VcsCommitMetadata> {
    VcsLogProvider::class.java.methods.find { it.name == "readMetadata" }?.also {
      it.isAccessible = true

      when {
        // 203.4203.26+
        // (VirtualFile, List<String>, Consumer<? super VcsCommitMetadata>) -> void
        it.returnType == Void.TYPE && it.parameterCount == 3 -> {
          val commitsMetadata = ArrayList<VcsCommitMetadata>(100)
          it.invoke(this, root, commitsHashes, Consumer(commitsMetadata::add))
          return commitsMetadata
        }
        // (VirtualFile, List<String>) -> List<VcsCommitMetadata>
        it.returnType == List::class.java && it.parameterCount == 2 -> {
          @Suppress("unchecked_cast")
          return it.invoke(this, root, commitsHashes) as List<VcsCommitMetadata>
        }
      }
    }

    return emptyList()
  }

  private fun getVcsLogProviders(): Map<VirtualFile, VcsLogProvider> {
    @Compatibility(minVersion = "201.3803.32", replaceWith = "VcsProjectLog.getLogProviders(Project)")
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
        EmptyConsumer.getInstance(),
        progressIndicator,
      )
    }

    return commitsMetadata
  }

  @Suppress("unused")
  @Compatibility(minVersion = "203.3645.34")
  private fun ensureLogCreated(): Boolean {
    val method =
      VcsProjectLog::class.java.getDeclaredMethod("ensureLogCreated", Project::class.java)   // 203.3645.34+
      ?: VcsProjectLog::class.java.getDeclaredMethod("getOrCreateLog", Project::class.java)  // 201.3803.32+
      ?: VcsLogContentUtil::class.java.getDeclaredMethod("getOrCreateLog", Project::class.java)
      ?: return false

    return try {
      method.isAccessible = true
      method.invoke(null, project)
      true
    } catch (ignored: Exception) {
      false
    }
  }
}
