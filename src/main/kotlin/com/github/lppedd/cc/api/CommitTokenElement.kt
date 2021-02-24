package com.github.lppedd.cc.api

import com.intellij.ui.JBColor
import java.awt.Color
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
abstract class CommitTokenElement @JvmOverloads constructor(
    val text: String,
    val description: String,
    val value: String = text,
) {
  open fun getRendering(): CommitTokenRendering =
    CommitTokenRendering()

  companion object {
    @JvmField
    val FOREGROUND: JBColor = JBColor.namedColor("CompletionPopup.foreground", JBColor.foreground())
  }

  data class CommitTokenRendering @JvmOverloads constructor(
      val bold: Boolean = false,
      val italic: Boolean = false,
      val strikeout: Boolean = false,
      val foreground: Color = FOREGROUND,
      val type: String? = null,
      val icon: Icon? = null,
      val hasCustomDocumentation: Boolean = false,
  )
}
