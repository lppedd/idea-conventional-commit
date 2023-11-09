package com.github.lppedd.cc.language

import com.github.lppedd.cc.CCIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
public object ConventionalCommitFileType : LanguageFileType(ConventionalCommitLanguage) {
  override fun getName(): String =
    language.displayName

  override fun getDescription(): String =
    language.displayName

  override fun getDefaultExtension(): String =
    "conventionalcommit"

  override fun getIcon(): Icon =
    CCIcons.Logo
}
