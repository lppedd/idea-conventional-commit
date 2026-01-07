package com.github.lppedd.cc.editor

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.ex.ActionRuntimeRegistrar
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer.LightCustomizeStrategy
import com.intellij.openapi.extensions.PluginId

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CommitTabActionConfigurationCustomizer : ActionConfigurationCustomizer, LightCustomizeStrategy {
  override suspend fun customize(actionRegistrar: ActionRuntimeRegistrar) {
    // Rider register its own EditorTab action, see com.jetbrains.rider.editorActions.FrontendTabAction.
    // Unfortunately, that action is coded in Kotlin and marked as final,
    // so the only way to avoid breaking Rider is simply to not offer
    // enhanced tabbing in the commit dialog
    if (PluginManager.isPluginInstalled(PluginId.getId("com.intellij.modules.rider"))) {
      return
    }

    val oldAction = actionRegistrar.getActionOrStub("EditorTab")

    if (oldAction != null) {
      val newAction = CommitTabAction()
      newAction.copyFrom(oldAction) // ActionUtil.copyFrom does not work
      actionRegistrar.replaceAction("EditorTab", newAction)
    }
  }
}
