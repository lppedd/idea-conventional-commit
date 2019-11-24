package com.github.lppedd.cc.icon

import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.CCIcons
import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class CCConfigFileIconProvider : IconProvider(), DumbAware {
  override fun getIcon(psiElement: PsiElement, flags: Int): Icon? {
    return psiElement.containingFile?.run {
      if (name == CCConstants.DEFAULT_FILE) {
        CCIcons.DEFAULT_PRESENTATION
      } else {
        null
      }
    }
  }
}
