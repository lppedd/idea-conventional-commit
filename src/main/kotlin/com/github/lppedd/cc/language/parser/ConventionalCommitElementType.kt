package com.github.lppedd.cc.language.parser

import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitElementType(debugName: String) : IElementType(debugName, ConventionalCommitLanguage) {
  companion object {
    @JvmField val COMMIT_MESSAGE: IElementType = ConventionalCommitElementType("COMMIT_MESSAGE")
    @JvmField val SCOPE: IElementType = ConventionalCommitElementType("SCOPE")
    @JvmField val FOOTER: IElementType = ConventionalCommitElementType("FOOTER")
  }
}
