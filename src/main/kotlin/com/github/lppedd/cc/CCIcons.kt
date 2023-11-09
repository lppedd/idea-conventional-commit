package com.github.lppedd.cc

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
public object CCIcons {
  @JvmField public val Logo: Icon = getIcon("logo.svg")

  public object General {
    @JvmField public val ArrowRight: Icon = getIcon("general/arrowRight.svg")
    @JvmField public val ClearMessageHistory: Icon = getIcon("general/clearMessageHistory.svg")
  }

  public object Provider {
    @JvmField public val Unknown: Icon = getIcon("provider/unknown.svg")
    @JvmField public val Disabled: Icon = getIcon("provider/disabled.svg")
    @JvmField public val Vcs: Icon = getIcon("provider/vcs.svg")
  }

  public object Tokens {
    @JvmField public val Type: Icon = getIcon("tokens/commitType.svg")
    @JvmField public val Scope: Icon = getIcon("tokens/commitScope.svg")
    @JvmField public val Subject: Icon = getIcon("tokens/commitDescription.svg")
    @JvmField public val Body: Icon = getIcon("tokens/commitBody.svg")
    @JvmField public val Footer: Icon = getIcon("tokens/commitFooter.svg")
  }

  public object FileTypes {
    @JvmField public val Generic: Icon = getIcon("fileTypes/generic.svg")
    @JvmField public val Json: Icon = getIcon("fileTypes/json.svg")
    @JvmField public val CoAuthors: Icon = getIcon("fileTypes/coauthors.svg")
  }

  private fun getIcon(path: String) =
    IconLoader.getIcon("/icons/$path", CCIcons::class.java)
}
