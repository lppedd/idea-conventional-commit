package com.github.lppedd.cc.icon

import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.configuration.CCConfigService
import com.intellij.ide.IconProvider
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.util.PathUtil
import javax.swing.Icon

/**
 * Provides an icon for the currently in use custom default tokens file, if any.
 *
 * It could be:
 *  - a custom JSON file picked via settings
 *  - a `conventionalcommit.json` file in the project's root directory
 *
 * @author Edoardo Luppi
 */
internal class CCConfigFileIconProvider : IconProvider(), DumbAware {
  override fun getIcon(psiElement: PsiElement, flags: Int): Icon? {
    if (psiElement !is JsonFile) {
      return null
    }

    val customFilePath = CCConfigService.getInstance(psiElement.project).customFilePath

    return if (
      isCustomFile(customFilePath, psiElement) ||
      isDefaultFile(customFilePath, psiElement)
    ) {
      CCIcons.DEFAULT_PRESENTATION
    } else {
      null
    }
  }

  /**
   * Checks if the file is an explicit custom file provided by the user via settings.
   */
  private fun isCustomFile(customFilePath: String?, psiFile: JsonFile): Boolean =
    customFilePath != null &&
    psiFile.virtualFile.path == PathUtil.toSystemIndependentName(customFilePath)

  /**
   * Checks iif the file is the implicit `conventionalcommit.json`
   * in the project's root directory.
   */
  private fun isDefaultFile(customFilePath: String?, psiFile: JsonFile): Boolean {
    if (customFilePath != null) {
      return false
    }

    val virtualFile = psiFile.virtualFile
    return virtualFile.name == CCConstants.DEFAULT_FILE &&
           virtualFile.parent.path == psiFile.project.basePath
  }
}
