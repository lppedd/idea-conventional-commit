package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCIcons
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
data class ProviderPresentation @JvmOverloads constructor(
    val name: String,
    val icon: Icon,
    val disabledIcon: Icon = CCIcons.Provider.Disabled,
)
