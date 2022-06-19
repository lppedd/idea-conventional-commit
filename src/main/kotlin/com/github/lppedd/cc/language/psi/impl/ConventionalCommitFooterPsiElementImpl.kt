package com.github.lppedd.cc.language.psi.impl

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.github.lppedd.cc.language.psi.ConventionalCommitFooterPsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitPsiElementVisitor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitFooterPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ConventionalCommitFooterPsiElement {
  override fun getFooterType(): String {
    val node = node.findChildByType(ConventionalCommitTokenType.FOOTER_TYPE)!!
    return node.text
  }

  override fun getFooterValue(): String? {
    val node = node.findChildByType(ConventionalCommitTokenType.FOOTER_VALUE)
    return node?.text
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is ConventionalCommitPsiElementVisitor) {
      visitor.visitFooter(this)
    } else {
      super.accept(visitor)
    }
  }
}
