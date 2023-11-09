package com.github.lppedd.cc.inspection

import com.intellij.vcs.commit.message.BaseCommitMessageInspection

/**
 * @author Edoardo Luppi
 */
public abstract class CommitBaseInspection : BaseCommitMessageInspection() {
  final override fun getGroupDisplayName(): String =
    super.getGroupDisplayName()

  final override fun getStaticDescription(): String? =
    super.getStaticDescription()

  @Suppress("exposed_super_class")
  public object ConventionalCommitReformatQuickFix : ReformatCommitMessageQuickFix()
}
