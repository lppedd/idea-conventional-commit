package com.github.lppedd.cc.api

import java.awt.Color
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
public interface TokenPresentation {
  /**
   * Returns whether the token text should be presented in bold.
   */
  public fun isBold(): Boolean =
    false

  /**
   * Returns whether the token text should be presented in italic.
   */
  public fun isItalic(): Boolean =
    false

  /**
   * Returns whether the token text should be presented as strike-through.
   */
  public fun isStrikeThrough(): Boolean =
    false

  /**
   * Returns the token text foreground color.
   */
  public fun getForeground(): Color? =
    null

  /**
   * Returns the optional text to display on the right side of the token.
   *
   * Examples are: "Recently used", "VCS"
   */
  public fun getType(): String? =
    null

  /**
   * Returns the optional icon to display on the right side of the token, next to the [type][getType].
   */
  public fun getIcon(): Icon? =
    null

  /**
   * Returns whether the token uses an entirely custom documentation format.
   *
   * TODO: clarify what we mean by custom.
   */
  public fun hasCustomDocumentation(): Boolean =
    false
}
