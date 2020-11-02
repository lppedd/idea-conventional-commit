package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCIcons
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapper.IdeModalityType
import com.intellij.openapi.ui.impl.DialogWrapperPeerImpl
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil
import java.awt.Component
import java.awt.Container
import javax.swing.Icon
import javax.swing.JComponent

/**
 * Hacks the dialog icon to set the Conventional Commit one.
 *
 * @author Edoardo Luppi
 */
internal class CCDialogWrapperPeer(
    wrapper: DialogWrapper,
    project: Project?,
    canBeParent: Boolean,
    ideModalityType: IdeModalityType,
) : DialogWrapperPeerImpl(wrapper, project, canBeParent, ideModalityType) {
  // @formatter:off
  private val customFrameDialogContentClass = Class.forName("com.intellij.openapi.wm.impl.customFrameDecorations.header.CustomFrameDialogContent")
  private val customHeaderClass = Class.forName("com.intellij.openapi.wm.impl.customFrameDecorations.header.CustomHeader")
  // @formatter:on

  override fun setContentPane(content: JComponent) {
    super.setContentPane(content)
    hackDialogIcon()
  }

  private fun hackDialogIcon() {
    val cp = contentPane as Any? ?: return

    when {
      customFrameDialogContentClass.isInstance(cp) -> {
        val customHeader = getCustomHeader(cp)
        setIcon(customHeader)
      }
      cp is Container -> {
        val customHeader = findCustomHeader(cp) ?: return
        setIcon(customHeader)
      }
    }
  }

  private fun findCustomHeader(container: Container): Component? {
    for (i in 0 until container.componentCount) {
      val component = container.getComponent(i)

      if (customHeaderClass.isInstance(component)) {
        return component
      }
    }

    return null
  }

  private fun getCustomHeader(customFrameDialogContent: Any): Any =
    customFrameDialogContentClass.getDeclaredField("header").let {
      it.isAccessible = true
      it.get(customFrameDialogContent)
    }

  private fun setIcon(customHeader: Any) {
    customHeaderClass.getDeclaredField("myIconProvider").let {
      it.isAccessible = true
      it.set(customHeader, ScaleContext.Cache { scaleAndGetIcon() })
    }
  }

  private fun scaleAndGetIcon(): Icon {
    val scale = 16 * UISettings.defFontScale / CCIcons.Logo.iconWidth
    return IconUtil.scale(CCIcons.Logo, null, scale)
  }
}
