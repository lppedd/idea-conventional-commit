package com.github.lppedd.cc.completion

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.weigher.CommitScopeElementWeigher
import com.github.lppedd.cc.completion.weigher.CommitSubjectElementWeigher
import com.github.lppedd.cc.completion.weigher.CommitTypeElementWeigher
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.lookupElement.TemplateCommitTypeLookupElement
import com.github.lppedd.cc.psi.CommitScopePsiElement
import com.github.lppedd.cc.psi.CommitSubjectPsiElement
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.openapi.util.Couple
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.util.ProcessingContext
import com.intellij.util.concurrency.Semaphore
import com.github.lppedd.cc.api.CommitScopeProvider.Companion.EP_NAME as SCOPE_EP
import com.github.lppedd.cc.api.CommitSubjectProvider.Companion.EP_NAME as SUBJECT_EP
import com.github.lppedd.cc.api.CommitTypeProvider.Companion.EP_NAME as TYPE_EP

/**
 * Provides context-based completion items inside the VCS commit dialog.
 *
 * @author Edoardo Luppi
 */
internal class CommitCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    // Completion items must be provided only for explicit completion invocation
    // (e.g. by using keyboard completion shortcuts)
    if (!parameters.isAutoPopup && parameters.invocationCount < 1) {
      return
    }

    val file = parameters.originalFile
    val project = file.project
    val document = PsiDocumentManager.getInstance(project).getDocument(file)

    // Items must be provided only inside the VCS commit dialog,
    // not in all generic text documents
    if (document?.getUserData(CommitMessage.DATA_KEY) == null) {
      return
    }

    val psiManager = PsiManager.getInstance(project)
    val prefix = TextFieldWithAutoCompletionListProvider.getCompletionPrefix(parameters)
    val config = CCConfigService.getInstance(project)

    val resultSet = result
      .caseInsensitive()
      .withPrefixMatcher(PlainPrefixMatcher(prefix))

    // If the user configured commit messages to be completed via templates,
    // we provide special `LookupElement`s which programmatically initiate
    // a `Template` instance on insertion
    if (config.completionType == CompletionType.TEMPLATE) {
      if (!parameters.isAutoPopup) {
        resultSet
          .withRelevanceSorter(sorter(CommitTypeElementWeigher))
          .also { rs ->
            TYPE_EP.getExtensions(project)
              .asSequence()
              .sortedBy(config::getProviderOrder)
              .flatMap { runWithCheckCanceled { it.getCommitTypes("") }.asSequence() }
              .map { CommitTypePsiElement(it, psiManager) }
              .mapIndexed(::TemplateCommitTypeLookupElement)
              .distinctBy(TemplateCommitTypeLookupElement::getLookupString)
              .forEach(rs::addElement)
          }
      }

      return
    }

    val caretOffset = parameters.editor.caretModel.logicalPosition.column
    val textPrecedingCaret = CCEditorUtils.getCurrentLineUntilCaret(parameters.editor)
    val commitTokens = CCParser.parseText(textPrecedingCaret)
    val subjectCtx = trySubjectContext(commitTokens)

    if (subjectCtx != null) {
      resultSet
        .withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
        .withRelevanceSorter(sorter(CommitSubjectElementWeigher))
        .also { rs ->
          safelyReleaseSemaphore(parameters.process)
          SUBJECT_EP.getExtensions(project)
            .asSequence()
            .sortedBy(config::getProviderOrder)
            .flatMap {
              runWithCheckCanceled {
                it.getCommitSubjects(subjectCtx.first, subjectCtx.second)
              }.asSequence()
            }
            .map { CommitSubjectPsiElement(it, psiManager) }
            .mapIndexed(::CommitSubjectLookupElement)
            .distinctBy(CommitSubjectLookupElement::getLookupString)
            .forEach(rs::addElement)
        }

      return
    }

    val scopeCtx = tryScopeContext(caretOffset - 1, commitTokens)

    if (scopeCtx != null) {
      val (type, scope) = scopeCtx
      resultSet
        .withPrefixMatcher(scope ?: return)
        .withRelevanceSorter(sorter(CommitScopeElementWeigher))
        .also { rs ->
          safelyReleaseSemaphore(parameters.process)
          SCOPE_EP.getExtensions(project)
            .asSequence()
            .sortedBy(config::getProviderOrder)
            .flatMap { runWithCheckCanceled { it.getCommitScopes(type) }.asSequence() }
            .map { CommitScopePsiElement(it, psiManager) }
            .mapIndexed(::CommitScopeLookupElement)
            .distinctBy(CommitScopeLookupElement::getLookupString)
            .forEach(rs::addElement)
        }

      return
    }

    val typeValue = tryTypeContext(textPrecedingCaret, caretOffset, commitTokens.type)
    resultSet
      .withPrefixMatcher(typeValue)
      .withRelevanceSorter(sorter(CommitTypeElementWeigher))
      .also { rs ->
        safelyReleaseSemaphore(parameters.process)
        TYPE_EP.getExtensions(project)
          .asSequence()
          .sortedBy(config::getProviderOrder)
          .flatMap { runWithCheckCanceled { it.getCommitTypes(typeValue) }.asSequence() }
          .map { CommitTypePsiElement(it, psiManager) }
          .mapIndexed(::CommitTypeLookupElement)
          .distinctBy(CommitTypeLookupElement::getLookupString)
          .forEach(rs::addElement)
      }
  }

  private fun sorter(weigher: LookupElementWeigher): CompletionSorter {
    val sorter = CompletionSorter.emptySorter() as CompletionSorterImpl
    return sorter
      .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
      .withClassifier(CompletionSorterImpl.weighingFactory(weigher))
  }

  /**
   * Checks if the caret is positioned inside the commit **type** context.
   *
   * @return a string representing the possibly partial commit type
   */
  private fun tryTypeContext(text: String, caretOffset: Int, type: PCommitType): String {
    return if (type.isInContext(caretOffset)) {
      type.value
    } else {
      text.trim()
    }
  }

  /**
   * Checks if the caret is positioned inside the commit **scope** context.
   *
   * @return a couple of strings, representing the commit type (first) and
   *         the possibly partial commit scope (second), or `null` if the
   *         caret isn't in context
   */
  private fun tryScopeContext(caretOffset: Int, commitTokens: PCommitTokens): Couple<String>? =
    if (commitTokens.scope.isInContext(caretOffset)) {
      Couple(commitTokens.type.value, commitTokens.scope.value)
    } else {
      null
    }

  /**
   * Checks if the caret is positioned inside the commit **subject** context.
   *
   * @return a couple of strings, representing the commit type (first) and
   *         the commit scope (second), or `null` if the caret isn't in context
   */
  private fun trySubjectContext(commitTokens: PCommitTokens): Couple<String>? =
    if (commitTokens.separator) {
      Couple.of(commitTokens.type.value, commitTokens.scope.value)
    } else {
      null
    }

  /**
   * This is a workaround to a standard behavior which allows the UI
   * to freeze for about 2 seconds if the first `LookupElement` isn't
   * immediately added to the Lookup.
   *
   * This allow for auto-inserting a single element without confusing
   * the user by showing it, but as our `LookupElement`s never auto-complete
   * we can get rid of it.
   */
  private fun safelyReleaseSemaphore(process: CompletionProcess) {
    @Suppress("DEPRECATION")
    if (process is CompletionProgressIndicator) {
      try {
        val semaphore = CCReflectionUtils.getField("myFreezeSemaphore", process) as Semaphore
        semaphore.up()
      } catch (ignored: Exception) {
        // Let's just continue
      }
    }
  }
}
