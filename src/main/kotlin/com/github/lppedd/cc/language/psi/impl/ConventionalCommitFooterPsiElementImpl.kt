package com.github.lppedd.cc.language.psi.impl

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.github.lppedd.cc.language.psi.ConventionalCommitFooterPsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitFooterTypePsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitFooterValuePsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitPsiElementVisitor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitFooterPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ConventionalCommitFooterPsiElement {
  override fun getType(): ConventionalCommitFooterTypePsiElement =
    findNotNullChildByType(ConventionalCommitTokenType.FOOTER_TYPE)

  override fun getValue(): ConventionalCommitFooterValuePsiElement? =
    findChildByType(ConventionalCommitTokenType.FOOTER_VALUE)

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is ConventionalCommitPsiElementVisitor) {
      visitor.visitFooter(this)
    } else {
      super.accept(visitor)
    }
  }
}
