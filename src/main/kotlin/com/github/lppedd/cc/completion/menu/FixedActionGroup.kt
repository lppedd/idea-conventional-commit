package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.APP_NAME
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.POPUP
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.HIDE_SELECTED
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.KEEP_SELECTED
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.icons.AllIcons.General
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.DumbAware
import com.intellij.util.ui.UIUtil
import java.util.*

/**
 * @author Edoardo Luppi
 */
internal class FixedActionGroup(private val lookup: LookupImpl) : ActionGroup("", false), DumbAware {
  private val config = CCConfigService.getInstance(lookup.project)
  private val commandProcessor = CommandProcessor.getInstance()
  private val actions = arrayOf(
    Separator.create(),
    CompletionModeChangerAction(),
    FilterModeChangerAction(),
    Separator.create(),
  )

  override fun getChildren(e: AnActionEvent?): Array<AnAction> =
    actions

  override fun equals(other: Any?): Boolean =
    other is FixedActionGroup && lookup === other.lookup

  override fun hashCode(): Int =
    Objects.hashCode(lookup)

  private fun invokeCompletion() {
    val project = lookup.project
    val editor = lookup.editor
    val command = Runnable {
      val invokedExplicitly = ApplicationManager.getApplication().isUnitTestMode
      CodeCompletionHandlerBase
        .createHandler(BASIC, invokedExplicitly, !invokedExplicitly, true)
        .invokeCompletion(project, editor, 1)
    }

    commandProcessor.executeCommand(project, command, "Invoke completion", APP_NAME)
  }

  private inner class CompletionModeChangerAction : AnAction(General.Settings) {
    override fun actionPerformed(e: AnActionEvent) {
      val current = config.completionType
      config.completionType = if (current == TEMPLATE) POPUP else TEMPLATE
      invokeCompletion()
    }

    override fun update(e: AnActionEvent) {
      val current = config.completionType
      e.presentation.text = if (current == TEMPLATE) {
        "Template ${UIUtil.rightArrow()} Standard"
      } else {
        "Standard ${UIUtil.rightArrow()} Template"
      }
    }
  }

  private inner class FilterModeChangerAction : AnAction(General.Filter) {
    override fun actionPerformed(e: AnActionEvent) {
      val current = config.providerFilterType
      config.providerFilterType = if (current == HIDE_SELECTED) KEEP_SELECTED else HIDE_SELECTED
      invokeCompletion()
    }

    override fun update(e: AnActionEvent) {
      val current = config.providerFilterType
      val hideSelected = CCBundle["cc.config.popup.hideSelected"]
      val keepSelected = CCBundle["cc.config.popup.keepSelected"]
      e.presentation.text = if (current == HIDE_SELECTED) {
        "$hideSelected ${UIUtil.rightArrow()} $keepSelected"
      } else {
        "$keepSelected ${UIUtil.rightArrow()} $hideSelected"
      }
    }
  }
}
