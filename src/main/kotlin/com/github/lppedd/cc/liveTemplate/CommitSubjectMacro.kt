package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.completion.providers.SubjectCompletionProvider
import com.github.lppedd.cc.completion.resultset.LookupResultSet
import com.github.lppedd.cc.parser.CommitContext.SubjectCommitContext
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CommitSubjectMacro : CommitMacro() {
  override fun getName() =
    "commitSubject"

  override fun queryProviders(project: Project, lookup: LookupImpl) {
    val commitContext = SubjectCommitContext("", "", "")
    SubjectCompletionProvider(project, commitContext).complete(LookupResultSet(lookup))
  }
}
