package com.github.lppedd.cc

import com.intellij.openapi.util.IconLoader

/**
 * @author Edoardo Luppi
 */
object CCIcons {
  @JvmField val UNKNOWN_PROVIDER = IconLoader.getIcon("/icons/unknownProvider.svg")
  @JvmField val DEFAULT_PRESENTATION = IconLoader.getIcon("/icons/defaultPresentation.svg")
  @JvmField val TYPE = IconLoader.getIcon("/icons/commitType.svg")
  @JvmField val SCOPE = IconLoader.getIcon("/icons/commitScope.svg")
  @JvmField val SUBJECT = IconLoader.getIcon("/icons/commitDescription.svg")
  @JvmField val ARROW_RIGHT = IconLoader.getIcon("/icons/arrowRight.svg")
}
