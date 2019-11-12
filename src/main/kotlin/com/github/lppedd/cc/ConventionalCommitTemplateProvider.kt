package com.github.lppedd.cc

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitTemplateProvider : DefaultLiveTemplatesProvider {
  override fun getDefaultLiveTemplateFiles() = emptyArray<String>()
  override fun getHiddenLiveTemplateFiles() = arrayOf("liveTemplates/ConventionalCommit")
}
