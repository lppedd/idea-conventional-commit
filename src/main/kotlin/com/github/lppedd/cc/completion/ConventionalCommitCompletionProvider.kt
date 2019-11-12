package com.github.lppedd.cc.completion

import com.github.lppedd.cc.CommitTokens
import com.github.lppedd.cc.CommitType
import com.github.lppedd.cc.ConventionalCommitParser
import com.github.lppedd.cc.completion.weighers.CommitScopeElementWeigher
import com.github.lppedd.cc.completion.weighers.CommitSubjectElementWeigher
import com.github.lppedd.cc.completion.weighers.CommitTypeElementWeigher
import com.github.lppedd.cc.extensions.ConventionalCommitProvider
import com.github.lppedd.cc.extensions.DefaultConventionalCommitProvider
import com.github.lppedd.cc.lookup.CommitScopeLookupElement
import com.github.lppedd.cc.lookup.CommitSubjectLookupElement
import com.github.lppedd.cc.lookup.CommitTypeLookupElement
import com.github.lppedd.cc.psi.CommitScopePsiElement
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.util.ProcessingContext
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitCompletionProvider : CompletionProvider<CompletionParameters>() {
  private val doAccept = AtomicBoolean(false)
  private val extensions = LinkedList(ConventionalCommitProvider.EP_NAME.extensionList)

  init {
    extensions.push(DefaultConventionalCommitProvider)
  }

  fun accept() {
    doAccept.compareAndSet(true, false)
  }

  fun block() {
    doAccept.compareAndSet(false, true)
  }

  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    if (!parameters.isAutoPopup && (parameters.invocationCount < 1 || doAccept.get())) {
      return
    }

    val file = parameters.originalFile
    val project = file.project
    val document = PsiDocumentManager.getInstance(project).getDocument(file)

    if (document?.getUserData(CommitMessage.DATA_KEY) == null) {
      return
    }

    val psiManager = PsiManager.getInstance(project)
    val prefix = TextFieldWithAutoCompletionListProvider.getCompletionPrefix(parameters)
    val resultSet = result
      .caseInsensitive()
      .withPrefixMatcher(PlainPrefixMatcher(prefix))

    val caretOffset = parameters.editor.caretModel.offset
    val lineNumber = document.getLineNumber(caretOffset)
    val lineStartOffset = document.getLineStartOffset(lineNumber)
    val textPrecedingCaret = document.text.substring(lineStartOffset, caretOffset)
    val commitTokens = ConventionalCommitParser.parseText(textPrecedingCaret)

    val subjectCtx = isInSubjectContext(commitTokens)

    if (subjectCtx != null) {
      val rs = resultSet
        .withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
        .withRelevanceSorter(defaultSorter(CommitSubjectElementWeigher, parameters, ""))

      extensions
        .flatMap { it.getCommitSubjects(project, subjectCtx.first, subjectCtx.second) }
        .map { CommitSubjectPsiElement(it.text, psiManager) }
        .mapIndexed { i, psi -> CommitSubjectLookupElement(i, psi) }
        .forEach { rs.addElement(it) }
      return
    }

    val scopeCtx = isInScopeContext(caretOffset, commitTokens)

    if (scopeCtx != null) {
      val (type, scope) = scopeCtx
      val rs = resultSet
        .withPrefixMatcher(scope)
        .withRelevanceSorter(defaultSorter(CommitScopeElementWeigher, parameters, type))

      extensions
        .flatMap { it.getCommitScopes(type) }
        .map { CommitScopePsiElement(it.name, psiManager) }
        .mapIndexed { i, psi -> CommitScopeLookupElement(i, psi) }
        .forEach { rs.addElement(it) }
      return
    }

    val typeStr = isInTypeContext(textPrecedingCaret, caretOffset, commitTokens.type)

    if (typeStr != null) {
      val rs = resultSet
        .withPrefixMatcher(typeStr)
        .withRelevanceSorter(defaultSorter(CommitTypeElementWeigher, parameters, typeStr))

      extensions
        .flatMap { it.getCommitTypes() }
        .map { CommitTypePsiElement(it.name, it.description, psiManager) }
        .mapIndexed { i, psi -> CommitTypeLookupElement(i, psi) }
        .forEach { rs.addElement(it) }
      return
    }
  }

  private fun defaultSorter(
    weigher: LookupElementWeigher,
    parameters: CompletionParameters,
    prefix: String
  ): CompletionSorterImpl {
    val sorter = CompletionSorter.defaultSorter(parameters, PlainPrefixMatcher(prefix)) as CompletionSorterImpl
    return sorter.withClassifier(CompletionSorterImpl.weighingFactory(weigher))
  }

  private fun isInTypeContext(text: String, caretOffset: Int, type: CommitType): String? {
    return if (type.isInContext(caretOffset)) {
      type.value
    } else {
      text.trim()
    }
  }

  private fun isInScopeContext(caretOffset: Int, commitTokens: CommitTokens): Pair<String, String>? =
    if (commitTokens.scope.isInContext(caretOffset)) {
      Pair(commitTokens.type.value, commitTokens.scope.value)
    } else {
      null
    }

  private fun isInSubjectContext(commitTokens: CommitTokens): Pair<String, String>? =
    if (commitTokens.separator) {
      Pair(commitTokens.type.value, commitTokens.scope.value)
    } else {
      null
    }
}
