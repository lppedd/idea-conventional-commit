package com.github.lppedd.cc.language

import com.github.lppedd.cc.language.psi.*
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitSpellcheckingStrategy : SpellcheckingStrategy() {
  private val tokenizer = object : Tokenizer<PsiElement>() {
    override fun tokenize(element: PsiElement, consumer: TokenConsumer) {
      consumer.consumeToken(element, PlainTextSplitter.getInstance())
    }
  }

  override fun getTokenizer(element: PsiElement): Tokenizer<*> =
    tokenizer

  override fun isMyContext(element: PsiElement): Boolean {
    return element is ConventionalCommitTypePsiElement ||
           element is ConventionalCommitScopePsiElement ||
           element is ConventionalCommitSubjectPsiElement ||
           element is ConventionalCommitBodyPsiElement ||
           element is ConventionalCommitFooterTypePsiElement ||
           element is ConventionalCommitFooterValuePsiElement
  }
}
