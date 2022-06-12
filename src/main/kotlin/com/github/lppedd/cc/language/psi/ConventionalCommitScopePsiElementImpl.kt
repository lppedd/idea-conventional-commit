package com.github.lppedd.cc.language.psi

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitScopePsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ConventionalCommitScopePsiElement {
  override fun hasClosingParenthesis(): Boolean =
    node.findChildByType(ConventionalCommitTokenType.PAREN_RIGHT) != null

  override fun getValue(): String {
    val valuePsiElement = node.findChildByType(ConventionalCommitTokenType.SCOPE)
    return valuePsiElement?.text ?: ""
  }

  override fun getNameIdentifier(): PsiElement? =
    findChildByType(ConventionalCommitTokenType.SCOPE)

  override fun setName(name: String): PsiElement {
    val valuePsiElement = node.findChildByType(ConventionalCommitTokenType.SCOPE) as LeafPsiElement?
    valuePsiElement?.replaceWithText(name)
    return this
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is ConventionalCommitPsiElementVisitor) {
      visitor.visitScope(this)
    } else {
      visitor.visitElement(this)
    }
  }
}
