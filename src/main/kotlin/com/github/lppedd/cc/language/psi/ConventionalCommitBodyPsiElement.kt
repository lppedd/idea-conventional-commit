package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiPlainText
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitBodyPsiElement(
    type: IElementType,
    text: CharSequence,
) : LeafPsiElement(type, text), PsiPlainText {
  override fun toString(): String =
    "ConventionalCommitBodyPsiElement"
}
