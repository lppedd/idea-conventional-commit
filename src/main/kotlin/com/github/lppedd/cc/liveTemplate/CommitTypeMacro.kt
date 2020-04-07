package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.completion.providers.TypeCompletionProvider
import com.github.lppedd.cc.completion.resultset.LookupResultSet
import com.github.lppedd.cc.parser.CommitContext.TypeCommitContext
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CommitTypeMacro : CommitMacro() {
  override fun getName() =
    "commitType"

  override fun queryProviders(project: Project, lookup: LookupImpl) {
    TypeCompletionProvider(project, TypeCommitContext("")).complete(LookupResultSet(lookup))
  }
}
