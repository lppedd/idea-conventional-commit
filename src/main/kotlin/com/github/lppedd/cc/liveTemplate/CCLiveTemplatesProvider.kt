package com.github.lppedd.cc.liveTemplate

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Edoardo Luppi
 */
private class CCLiveTemplatesProvider : DefaultLiveTemplatesProvider {
  override fun getDefaultLiveTemplateFiles() =
    emptyArray<String>()

  override fun getHiddenLiveTemplateFiles() =
    arrayOf("liveTemplates/ConventionalCommit")
}
