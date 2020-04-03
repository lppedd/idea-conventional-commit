package com.github.lppedd.cc.configuration.holders

import com.intellij.openapi.ui.ValidationInfo

/**
 * @author Edoardo Luppi
 */
internal interface Validatable {
  fun validate(): Collection<ValidationInfo>
}
