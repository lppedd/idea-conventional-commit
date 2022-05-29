package com.github.lppedd.cc.api

import java.awt.Color
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
interface TokenPresentation {
  /** Returns if the token's text should be presented in bold. */
  fun isBold(): Boolean =
    false

  /** Returns if the token's text should be presented in italic. */
  fun isItalic(): Boolean =
    false

  /** Returns if the token's text should be presented as strike-through. */
  fun isStrikeThrough(): Boolean =
    false

  /** Returns the token's text foreground color. */
  fun getForeground(): Color? =
    null

  fun getType(): String? =
    null

  fun getIcon(): Icon? =
    null

  fun hasCustomDocumentation(): Boolean =
    false
}
