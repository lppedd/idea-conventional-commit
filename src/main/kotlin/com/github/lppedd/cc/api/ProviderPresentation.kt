package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCIcons
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
interface ProviderPresentation {
  /**
   * Returns the provider's presentable name.
   *
   * The presentable name is used when the user must interact with the provider
   * in some way via the UI, such as in settings, or in the completion's popup.
   */
  fun getName(): String

  /**
   * Returns the icon that visually identify the provider.
   *
   * The icon is used in every context where the provider is presented in the UI,
   * such as in settings, or in the completion's popup.
   */
  fun getIcon(): Icon

  /**
   * Returns the icon that is used in case the provider has been disabled,
   * programmatically or by the user.
   *
   * @see getIcon
   */
  fun getDisabledIcon(): Icon =
    CCIcons.Provider.Disabled
}
