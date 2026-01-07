package com.github.lppedd.cc.editor

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionRuntimeRegistrar
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer.LightCustomizeStrategy
import com.intellij.openapi.extensions.PluginId

/**
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CommitTabActionConfigurationCustomizer : ActionConfigurationCustomizer, LightCustomizeStrategy {
  private val actionId = "EditorTab"
  private val groupId = "EditorActions"

  override suspend fun customize(actionRegistrar: ActionRuntimeRegistrar) {
    // Rider register its own EditorTab action, see com.jetbrains.rider.editorActions.FrontendTabAction.
    // Unfortunately, that action is coded in Kotlin and marked as final,
    // so the only way to avoid breaking Rider is simply to not offer
    // enhanced tabbing in the commit dialog
    if (PluginManager.isPluginInstalled(PluginId.getId("com.intellij.modules.rider"))) {
      return
    }

    val oldAction = actionRegistrar.getActionOrStub(actionId)

    if (oldAction != null) {
      val actionGroup = actionRegistrar.getActionOrStub(groupId) as DefaultActionGroup
      val newAction = CommitTabAction()
      actionRegistrar.unregisterAction(actionId)
      actionRegistrar.registerAction(actionId, newAction)
      actionRegistrar.addToGroup(actionGroup, newAction, Constraints.LAST)
    }
  }
}
