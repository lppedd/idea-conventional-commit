package com.github.lppedd.cc.liveTemplate

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Edoardo Luppi
 */
internal class CCLiveTemplatesProvider : DefaultLiveTemplatesProvider {
  override fun getDefaultLiveTemplateFiles() = emptyArray<String>()
  override fun getHiddenLiveTemplateFiles() = arrayOf("liveTemplates/ConventionalCommit")
}
