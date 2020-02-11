package com.github.lppedd.cc.icon

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.configuration.CCConfigService
import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.util.PathUtil
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class CCConfigFileIconProvider : IconProvider(), DumbAware {
  override fun getIcon(psiElement: PsiElement, flags: Int): Icon? {
    return CCConfigService.getInstance(psiElement.project).customFilePath?.let {
      psiElement.containingFile?.run {
        if (virtualFile.path == PathUtil.toSystemIndependentName(it)) {
          CCIcons.DEFAULT_PRESENTATION
        } else {
          null
        }
      }
    }
  }
}
