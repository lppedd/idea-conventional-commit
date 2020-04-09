package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.completion.providers.BodyCompletionProvider
import com.github.lppedd.cc.completion.resultset.LookupResultSet
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
private class CommitBodyMacro : CommitMacro() {
  override fun getName() =
    "commitBody"

  override fun queryProviders(project: Project, lookup: LookupImpl) {
    val commitContext = FooterTypeContext("")
    BodyCompletionProvider(project, commitContext, CommitTokens()).complete(LookupResultSet(lookup))
  }
}
