package com.github.lppedd.cc

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil.isDark
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color
import javax.swing.Icon as SwingIcon

/**
 * @author Edoardo Luppi
 */
@Suppress("ConstPropertyName")
public object CC {
  public const val PluginId: String = "com.github.lppedd.idea-conventional-commit"
  public const val AppName: String = "ConventionalCommit"

  public object File {
    public const val Defaults: String = "conventionalcommit.json"
    public const val Schema: String = "conventionalcommit.schema.json"
    public const val CoAuthors: String = "conventionalcommit.coauthors"
    public const val Settings: String = "conventionalCommit.xml"
  }

  public object Registry {
    public const val VcsEnabled: String = "com.github.lppedd.cc.providers.vcs"
  }

  public object Icon {
    @JvmField public val Logo: SwingIcon = icon("logo.svg")

    public object General {
      @JvmField public val ArrowRight: SwingIcon = icon("general/arrowRight.svg")
      @JvmField public val ClearMessageHistory: SwingIcon = icon("general/clearMessageHistory.svg")
    }

    public object Provider {
      @JvmField public val Unknown: SwingIcon = icon("provider/unknown.svg")
      @JvmField public val Disabled: SwingIcon = icon("provider/disabled.svg")
      @JvmField public val Vcs: SwingIcon = icon("provider/vcs.svg")
    }

    public object Token {
      @JvmField public val Type: SwingIcon = icon("token/commitType.svg")
      @JvmField public val Scope: SwingIcon = icon("token/commitScope.svg")
      @JvmField public val Subject: SwingIcon = icon("token/commitDescription.svg")
      @JvmField public val Body: SwingIcon = icon("token/commitBody.svg")
      @JvmField public val Footer: SwingIcon = icon("token/commitFooter.svg")
    }

    public object FileType {
      @JvmField public val Generic: SwingIcon = icon("fileType/generic.svg")
      @JvmField public val Json: SwingIcon = icon("fileType/json.svg")
      @JvmField public val CoAuthors: SwingIcon = icon("fileType/coauthors.svg")
    }

    private fun icon(path: String) = IconLoader.getIcon("/icons/$path", CC::class.java)
  }

  public object UI {
    @JvmField public val BorderColor: Color = JBColor.lazy {
      if (JBColor.isBright()) {
        JBColor.border()
      } else {
        val borderColor = JBColor.border()

        if (isDark(borderColor)) {
          UIUtil.getListBackground().brighter(0.75)
        } else {
          borderColor
        }
      }
    }

    @JvmField public val ListBackgroundColor: Color = JBColor.lazy {
      if (JBColor.isBright()) {
        UIUtil.getListBackground()
      } else {
        UIUtil.getListBackground().brighter(0.96)
      }
    }
  }
}
