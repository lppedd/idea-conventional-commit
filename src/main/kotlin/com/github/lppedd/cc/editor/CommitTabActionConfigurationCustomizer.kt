package com.github.lppedd.cc.editor

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer
import com.intellij.openapi.extensions.PluginId

/**
 * @author Edoardo Luppi
 */
internal class CommitTabActionConfigurationCustomizer : ActionConfigurationCustomizer {
  private val actionId = "EditorTab"
  private val groupId = "EditorActions"

  override fun customize(actionManager: ActionManager) {
    // Rider register its own EditorTab action, see com.jetbrains.rider.editorActions.FrontendTabAction.
    // Unfortunately that action is coded in Kotlin and marked as final,
    // so the only way to avoid breaking Rider is simply to not offer
    // enhanced tabbing in the commit dialog
    if (!PluginManager.isPluginInstalled(PluginId.getId("com.intellij.modules.rider"))) {
      val oldAction = actionManager.getActionOrStub(actionId)

      if (oldAction != null) {
        val actionGroup = actionManager.getAction(groupId) as DefaultActionGroup
        val newAction = CommitTabAction()
        actionManager.unregisterAction(actionId)
        actionManager.registerAction(actionId, newAction)

        // The input ActionManager must be passed in to avoid infinite recursion
        actionGroup.addAction(newAction, Constraints.LAST, actionManager)
      }
    }
  }
}
