package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCIcons
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
public interface ProviderPresentation {
  /**
   * Returns the provider's presentable name.
   *
   * The presentable name is used when the user must interact with the provider
   * in some way via the UI, such as in settings, or in the completion popup.
   */
  public fun getName(): String

  /**
   * Returns the icon that visually identifies the provider.
   *
   * The icon is used in every context where the provider is presented in the UI,
   * such as in settings, or in the completion popup.
   */
  public fun getIcon(): Icon

  /**
   * Returns the icon used in case the provider has been disabled,
   * programmatically or by the user.
   *
   * @see getIcon
   */
  public fun getDisabledIcon(): Icon =
    CCIcons.Provider.Disabled
}
