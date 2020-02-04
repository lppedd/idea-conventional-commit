package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.api.DefaultCommitTokenProvider.JsonCommitType
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

/**
 * @author Edoardo Luppi
 */
internal interface DefaultTokensFileChangeListener {
  companion object {
    internal val TOPIC = Topic.create(
      "Notification for ${CCConstants.DEFAULT_FILE} changes",
      DefaultTokensFileChangeListener::class.java
    )
  }

  fun fileChanged(project: Project, defaults: Map<String, JsonCommitType>)
}
