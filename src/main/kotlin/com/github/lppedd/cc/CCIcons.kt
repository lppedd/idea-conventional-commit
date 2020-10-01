@file:JvmName("CCIcons")

package com.github.lppedd.cc

import com.intellij.openapi.util.IconLoader.getIcon
import javax.swing.Icon

private val THIS_CLASS = Class.forName("com.github.lppedd.cc.CCIcons")

@JvmField val ICON_UNKNOWN_PROVIDER: Icon = getIcon("/icons/unknownProvider.svg", THIS_CLASS)
@JvmField val ICON_DEFAULT_PRESENTATION: Icon = getIcon("/icons/defaultPresentation.svg", THIS_CLASS)
@JvmField val ICON_TYPE: Icon = getIcon("/icons/commitType.svg", THIS_CLASS)
@JvmField val ICON_SCOPE: Icon = getIcon("/icons/commitScope.svg", THIS_CLASS)
@JvmField val ICON_SUBJECT: Icon = getIcon("/icons/commitDescription.svg", THIS_CLASS)
@JvmField val ICON_BODY: Icon = getIcon("/icons/commitBody.svg", THIS_CLASS)
@JvmField val ICON_FOOTER: Icon = getIcon("/icons/commitFooter.svg", THIS_CLASS)
@JvmField val ICON_ARROW_RIGHT: Icon = getIcon("/icons/arrowRight.svg", THIS_CLASS)
@JvmField val ICON_DISABLED: Icon = getIcon("/icons/defaultPresentationDisabled.svg", THIS_CLASS)
