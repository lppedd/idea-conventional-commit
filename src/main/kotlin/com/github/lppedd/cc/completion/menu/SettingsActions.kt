package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.POPUP
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.HIDE_SELECTED
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.KEEP_SELECTED
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.icons.AllIcons.General.Filter
import com.intellij.icons.AllIcons.General.Settings
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.util.ui.UIUtil
import java.util.*

/**
 * The group of actions which makes it possible to change plugin's settings
 * from the pop-up's menu.
 *
 * @author Edoardo Luppi
 */
internal class SettingsActions(
    private val enhancer: LookupEnhancerLookupListener,
    private val lookup: LookupImpl,
) : ActionGroup("", false), DumbAware {
  private val config = lookup.project.service<CCConfigService>()
  private val actions = arrayOf(
    Separator.create(),
    CompletionModeChangeAction(),
    FilterModeChangeAction(),
    Separator.create(),
  )

  override fun getChildren(e: AnActionEvent?): Array<AnAction> =
    actions

  override fun equals(other: Any?): Boolean =
    other is SettingsActions && lookup === other.lookup

  override fun hashCode(): Int =
    Objects.hashCode(lookup)

  private inner class CompletionModeChangeAction : AnAction(Settings) {
    override fun actionPerformed(e: AnActionEvent) {
      config.completionType =
        if (config.completionType == TEMPLATE) POPUP
        else TEMPLATE
      enhancer.settingChanged()
    }

    override fun update(e: AnActionEvent) {
      e.presentation.text = if (config.completionType == TEMPLATE) {
        "Template ${UIUtil.rightArrow()} Standard"
      } else {
        "Standard ${UIUtil.rightArrow()} Template"
      }
    }
  }

  private inner class FilterModeChangeAction : AnAction(Filter) {
    override fun actionPerformed(e: AnActionEvent) {
      config.providerFilterType =
        if (config.providerFilterType == HIDE_SELECTED) KEEP_SELECTED
        else HIDE_SELECTED
      enhancer.settingChanged()
    }

    override fun update(e: AnActionEvent) {
      val hideSelected = CCBundle["cc.config.popup.hideSelected"]
      val keepSelected = CCBundle["cc.config.popup.keepSelected"]
      e.presentation.text = if (config.providerFilterType == HIDE_SELECTED) {
        "$hideSelected ${UIUtil.rightArrow()} $keepSelected"
      } else {
        "$keepSelected ${UIUtil.rightArrow()} $hideSelected"
      }
    }
  }
}
