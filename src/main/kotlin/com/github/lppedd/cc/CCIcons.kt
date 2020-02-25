package com.github.lppedd.cc

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
object CCIcons {
  @JvmField val UNKNOWN_PROVIDER: Icon = IconLoader.getIcon("/icons/unknownProvider.svg")
  @JvmField val DEFAULT_PRESENTATION: Icon = IconLoader.getIcon("/icons/defaultPresentation.svg")
  @JvmField val TYPE: Icon = IconLoader.getIcon("/icons/commitType.svg")
  @JvmField val SCOPE: Icon = IconLoader.getIcon("/icons/commitScope.svg")
  @JvmField val SUBJECT: Icon = IconLoader.getIcon("/icons/commitDescription.svg")
  @JvmField val ARROW_RIGHT: Icon = IconLoader.getIcon("/icons/arrowRight.svg")
}
