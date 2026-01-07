package com.github.lppedd.cc.language.psi.impl

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.github.lppedd.cc.language.psi.ConventionalCommitPsiElementVisitor
import com.github.lppedd.cc.language.psi.ConventionalCommitScopePsiElement
import com.github.lppedd.cc.language.psi.ConventionalCommitScopeValuePsiElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitScopePsiElementImpl(node: ASTNode) :
    ASTWrapperPsiElement(node),
    ConventionalCommitScopePsiElement {
  override fun getValue(): ConventionalCommitScopeValuePsiElement? =
    findChildByType(ConventionalCommitTokenType.SCOPE)

  override fun hasClosingParenthesis(): Boolean =
    node.findChildByType(ConventionalCommitTokenType.SCOPE_CLOSE_PAREN) != null

  override fun getNameIdentifier(): PsiElement? =
    getValue()

  override fun setName(name: String): PsiElement {
    getValue()?.replaceWithText(name)
    return this
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is ConventionalCommitPsiElementVisitor) {
      visitor.visitScope(this)
    } else {
      super.accept(visitor)
    }
  }
}
