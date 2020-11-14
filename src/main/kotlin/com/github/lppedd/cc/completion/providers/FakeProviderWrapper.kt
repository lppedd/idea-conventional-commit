package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.completion.Priority

/**
 * @author Edoardo Luppi
 */
internal object FakeProviderWrapper : ProviderWrapper {
  override fun getPriority() = Priority(0)
  override fun getId() = ""
  override fun getPresentation() = ProviderPresentation("", CCIcons.Logo)
}
