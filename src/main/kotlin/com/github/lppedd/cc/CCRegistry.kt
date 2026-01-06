package com.github.lppedd.cc

import com.intellij.l10n.LocalizationUtil
import com.intellij.openapi.util.registry.Registry
import java.util.*

/**
 * @author Edoardo Luppi
 */
internal object CCRegistry {
  /**
   * Returns the locale used by the plugin UI.
   */
  fun getLocale(): Locale {
    val value = Registry.get(CC.Registry.Locale)
    return when (value.selectedOption) {
      "English" -> Locale.ROOT
      "Chinese (Simplified)" -> Locale.SIMPLIFIED_CHINESE
      else -> LocalizationUtil.getLocale()
    }
  }

  /**
   * Returns whether support for inspecting the VCS log is enabled.
   */
  fun isVcsSupportEnabled(): Boolean =
    Registry.`is`(CC.Registry.VcsEnabled, false)
}
