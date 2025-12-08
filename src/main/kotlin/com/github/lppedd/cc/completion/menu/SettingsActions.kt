package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.completion.LookupEnhancer
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.POPUP
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.HIDE_SELECTED
import com.github.lppedd.cc.configuration.CCConfigService.ProviderFilterType.KEEP_SELECTED
import com.github.lppedd.cc.updateIcons
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.util.ui.UIUtil
import java.util.*

/**
 * The group of actions which makes it possible to change the plugin's settings
 * from the pop-up's menu.
 *
 * @author Edoardo Luppi
 */
internal class SettingsActions(
    private val enhancer: LookupEnhancer,
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

  private inner class CompletionModeChangeAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread =
      ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
      config.completionType =
        if (config.completionType == TEMPLATE) {
          POPUP
        } else {
          TEMPLATE
        }

      enhancer.settingChanged()
    }

    override fun update(event: AnActionEvent) {
      event.presentation.also {
        it.updateIcons(AllIcons.General.Settings)
        it.text = if (config.completionType == TEMPLATE) {
          "Template ${UIUtil.rightArrow()} Standard"
        } else {
          "Standard ${UIUtil.rightArrow()} Template"
        }
      }
    }
  }

  private inner class FilterModeChangeAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread =
      ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
      config.providerFilterType =
        if (config.providerFilterType == HIDE_SELECTED) {
          KEEP_SELECTED
        } else {
          HIDE_SELECTED
        }

      enhancer.settingChanged()
    }

    override fun update(event: AnActionEvent) {
      val hideSelected = CCBundle["cc.completion.menu.filter.hideSelected"]
      val keepSelected = CCBundle["cc.completion.menu.filter.keepSelected"]

      event.presentation.also {
        it.updateIcons(AllIcons.General.Filter)
        it.text = if (config.providerFilterType == HIDE_SELECTED) {
          "$hideSelected ${UIUtil.rightArrow()} $keepSelected"
        } else {
          "$keepSelected ${UIUtil.rightArrow()} $hideSelected"
        }
      }
    }
  }
}
