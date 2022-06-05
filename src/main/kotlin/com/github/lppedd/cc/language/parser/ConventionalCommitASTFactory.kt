package com.github.lppedd.cc.language.parser

import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.BODY
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.FOOTER_TYPE
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.FOOTER_TYPE_BREAKING_CHANGE
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.FOOTER_VALUE
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.SCOPE
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.SUBJECT
import com.github.lppedd.cc.language.lexer.ConventionalCommitTokenType.Companion.TYPE
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
    tokensMap[TYPE] = ::ConventionalCommitTypePsiElement
    tokensMap[SCOPE] = ::ConventionalCommitScopePsiElement
    tokensMap[SUBJECT] = ::ConventionalCommitSubjectPsiElement
    tokensMap[BODY] = ::ConventionalCommitBodyPsiElement
    tokensMap[FOOTER_TYPE] = ::ConventionalCommitFooterTypePsiElement
    tokensMap[FOOTER_TYPE_BREAKING_CHANGE] = ::ConventionalCommitFooterTypePsiElement
    tokensMap[FOOTER_VALUE] = ::ConventionalCommitFooterValuePsiElement
  }

  override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
    val producer = tokensMap[type] ?: return super.createLeaf(type, text)
    return producer(type, text)
  }
}
