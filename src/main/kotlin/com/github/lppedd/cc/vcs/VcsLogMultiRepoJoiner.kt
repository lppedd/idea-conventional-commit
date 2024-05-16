package com.github.lppedd.cc.vcs

import com.intellij.vcs.log.graph.GraphCommit

/**
 * This is a copy of the IJ `VcsLogMultiRepoJoiner` class,
 * that has been made package private starting from 2024.2.
 */
internal class VcsLogMultiRepoJoiner<CommitId, Commit : GraphCommit<CommitId>> {
  fun join(logsFromRepos: Collection<List<Commit>>): List<Commit> {
    if (logsFromRepos.size == 1) {
      return logsFromRepos.first()
    }

    val result = ArrayList<Commit>(getInitialSize(logsFromRepos))
    val nextCommits = HashMap<Commit, Iterator<Commit>>()

    for (log in logsFromRepos) {
      val iterator = log.iterator()

      if (iterator.hasNext()) {
        nextCommits[iterator.next()] = iterator
      }
    }

    while (nextCommits.isNotEmpty()) {
      val lastCommit = findLatestCommit(nextCommits.keys)
      val iterator = nextCommits.getValue(lastCommit)

      result.add(lastCommit)
      nextCommits.remove(lastCommit)

      if (iterator.hasNext()) {
        nextCommits[iterator.next()] = iterator
      }
    }

    return result
  }

  private fun findLatestCommit(commits: Set<Commit>): Commit {
    var maxTimeStamp = Long.MIN_VALUE
    var lastCommit: Commit? = null

    for (commit in commits) {
      val timestamp = commit.timestamp

      if (timestamp >= maxTimeStamp) {
        maxTimeStamp = timestamp
        lastCommit = commit
      }
    }

    return checkNotNull(lastCommit)
  }

  private fun getInitialSize(logsFromRepos: Collection<List<Commit>>): Int {
    var size = 0

    for (repo in logsFromRepos) {
      size += repo.size
    }

    return size
  }
}
