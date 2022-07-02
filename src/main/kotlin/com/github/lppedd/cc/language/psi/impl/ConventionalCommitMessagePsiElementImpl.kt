package com.github.lppedd.cc.language.psi.impl

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.github.lppedd.cc.language.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitMessagePsiElementImpl(node: ASTNode) :
    ASTWrapperPsiElement(node),
    ConventionalCommitMessagePsiElement {
  override fun getType(): ConventionalCommitTypePsiElement? =
    findChildByType(ConventionalCommitTokenType.TYPE)

  override fun getScope(): ConventionalCommitScopePsiElement? =
    findChildByClass(ConventionalCommitScopePsiElement::class.java)

  override fun getSubject(): ConventionalCommitSubjectPsiElement? =
    findChildByType(ConventionalCommitTokenType.SUBJECT)

  override fun getBody(): ConventionalCommitBodyPsiElement? =
    findChildByType(ConventionalCommitTokenType.BODY)

  override fun getFooters(): Array<out ConventionalCommitFooterPsiElement> =
    findChildrenByClass(ConventionalCommitFooterPsiElement::class.java)
}
