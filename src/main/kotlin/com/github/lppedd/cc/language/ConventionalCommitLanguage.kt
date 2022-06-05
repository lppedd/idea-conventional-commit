package com.github.lppedd.cc.language

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.PlainTextLanguage

/**
 * @author Edoardo Luppi
 */
object ConventionalCommitLanguage : Language(PlainTextLanguage.INSTANCE, "ConventionalCommit") {
  override fun getDisplayName(): String =
    "Conventional Commit"

  override fun isCaseSensitive(): Boolean =
    false
}
