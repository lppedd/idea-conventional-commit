package com.github.lppedd.cc.language

import com.github.lppedd.cc.CCBundle
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.PlainTextLanguage

/**
 * @author Edoardo Luppi
 */
public object ConventionalCommitLanguage : Language(PlainTextLanguage.INSTANCE, "ConventionalCommit") {
  override fun getDisplayName(): String =
    CCBundle["cc.language.name"]

  override fun isCaseSensitive(): Boolean =
    false
}
