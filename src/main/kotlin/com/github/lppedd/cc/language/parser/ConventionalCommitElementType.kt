package com.github.lppedd.cc.language.parser

import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
public class ConventionalCommitElementType(debugName: String) : IElementType(debugName, ConventionalCommitLanguage) {
  public companion object {
    @JvmField public val COMMIT_MESSAGE: IElementType = ConventionalCommitElementType("COMMIT_MESSAGE")
    @JvmField public val SCOPE: IElementType = ConventionalCommitElementType("SCOPE")
    @JvmField public val FOOTER: IElementType = ConventionalCommitElementType("FOOTER")
  }
}
