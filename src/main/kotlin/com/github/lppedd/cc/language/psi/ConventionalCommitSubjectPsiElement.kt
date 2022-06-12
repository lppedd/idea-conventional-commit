package com.github.lppedd.cc.language.psi

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitSubjectPsiElement(type: IElementType, text: CharSequence) : LeafPsiElement(type, text) {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is ConventionalCommitPsiElementVisitor) {
      visitor.visitSubject(this)
    } else {
      visitor.visitElement(this)
    }
  }

  override fun toString(): String =
    "ConventionalCommitSubjectPsiElement"
}
