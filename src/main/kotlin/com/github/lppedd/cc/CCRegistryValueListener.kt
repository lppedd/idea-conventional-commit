package com.github.lppedd.cc

import com.intellij.openapi.util.registry.RegistryValue
import com.intellij.openapi.util.registry.RegistryValueListener

/**
 * @author Edoardo Luppi
 */
internal class CCRegistryValueListener : RegistryValueListener {
  override fun afterValueChanged(value: RegistryValue) {
    if (value.key == CC.Registry.Locale) {
      val locale = CCRegistry.getLocale()
      CCBundle.setLocale(locale)
    }
  }
}
