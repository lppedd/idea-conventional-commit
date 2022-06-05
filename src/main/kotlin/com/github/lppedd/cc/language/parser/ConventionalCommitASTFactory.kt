package com.github.lppedd.cc.language.parser

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType
import com.github.lppedd.cc.language.psi.*
import com.intellij.lang.ASTFactory
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.tree.IElementType
import java.util.*

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitASTFactory : ASTFactory() {
  private val tokensMap = IdentityHashMap<IElementType, (t: IElementType, c: CharSequence) -> LeafElement>(6)

  init {
    tokensMap[ConventionalCommitTokenType.TYPE] = ::ConventionalCommitTypePsiElement
    tokensMap[ConventionalCommitTokenType.SCOPE] = ::ConventionalCommitScopePsiElement
    tokensMap[ConventionalCommitTokenType.SUBJECT] = ::ConventionalCommitSubjectPsiElement
    tokensMap[ConventionalCommitTokenType.BODY] = ::ConventionalCommitBodyPsiElement
    tokensMap[ConventionalCommitTokenType.FOOTER_TYPE] = ::ConventionalCommitFooterTypePsiElement
    tokensMap[ConventionalCommitTokenType.FOOTER_VALUE] = ::ConventionalCommitFooterValuePsiElement
  }

  override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
    val producer = tokensMap[type] ?: return super.createLeaf(type, text)
    return producer(type, text)
  }
}
