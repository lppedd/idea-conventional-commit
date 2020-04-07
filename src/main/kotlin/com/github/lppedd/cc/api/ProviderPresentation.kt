package com.github.lppedd.cc.api

import com.github.lppedd.cc.ICON_DISABLED
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
data class ProviderPresentation @JvmOverloads constructor(
    val name: String,
    val icon: Icon,
    val disabledIcon: Icon = ICON_DISABLED,
)
