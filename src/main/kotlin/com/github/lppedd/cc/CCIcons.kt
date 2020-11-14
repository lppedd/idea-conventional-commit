package com.github.lppedd.cc

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
object CCIcons {
  @JvmField val Logo: Icon = getIcon("logo.svg")

  object General {
    @JvmField val ArrowRight: Icon = getIcon("general/arrowRight.svg")
  }

  object Provider {
    @JvmField val Unknown: Icon = getIcon("provider/unknown.svg")
    @JvmField val Disabled: Icon = getIcon("provider/disabled.svg")
    @JvmField val Recent: Icon = getIcon("provider/recent.svg")
    @JvmField val Vcs: Icon = getIcon("provider/vcs.svg")
  }

  object Tokens {
    @JvmField val Type: Icon = getIcon("tokens/commitType.svg")
    @JvmField val Scope: Icon = getIcon("tokens/commitScope.svg")
    @JvmField val Subject: Icon = getIcon("tokens/commitDescription.svg")
    @JvmField val Body: Icon = getIcon("tokens/commitBody.svg")
    @JvmField val Footer: Icon = getIcon("tokens/commitFooter.svg")
  }

  object FileTypes {
    @JvmField val Generic: Icon = getIcon("fileTypes/generic.svg")
    @JvmField val Json: Icon = getIcon("fileTypes/json.svg")
    @JvmField val CoAuthors: Icon = getIcon("fileTypes/coauthors.svg")
  }

  @JvmStatic
  private fun getIcon(path: String) = IconLoader.getIcon("/icons/$path", CCIcons::class.java)
}
