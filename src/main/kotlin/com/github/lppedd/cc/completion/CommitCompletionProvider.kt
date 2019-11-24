package com.github.lppedd.cc.completion

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.CommitTypeProvider
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
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.util.ProcessingContext
import com.intellij.util.concurrency.Semaphore

/**
 * @author Edoardo Luppi
 */
internal class CommitCompletionProvider : CompletionProvider<CompletionParameters>() {
  companion object {
    private val TYPE_EP = CommitTypeProvider.EP_NAME
    private val SCOPE_EP = CommitScopeProvider.EP_NAME
    private val SUBJECT_EP = CommitSubjectProvider.EP_NAME
  }

  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    if (!parameters.isAutoPopup && parameters.invocationCount < 1) {
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
    val config = ServiceManager.getService(project, CCConfigService::class.java)

    val resultSet = result
      .caseInsensitive()
      .withPrefixMatcher(PlainPrefixMatcher(prefix))

    if (config.completionType == CompletionType.TEMPLATE) {
      if (!parameters.isAutoPopup) {
        resultSet
          .withRelevanceSorter(sorter(CommitTypeElementWeigher))
          .also { rs ->
            TYPE_EP.getExtensions(project)
              .asSequence()
              .sortedBy { config.getProviderOrder(it) }
              .flatMap { runWithCheckCanceled { it.getCommitTypes("") }.asSequence() }
              .map { CommitTypePsiElement(it, psiManager) }
              .mapIndexed { i, psi -> TemplateCommitTypeLookupElement(i, psi) }
              .distinctBy { e -> e.lookupString }
              .forEach { rs.addElement(it) }
          }
      }

      return
    }

    val caretOffset = parameters.editor.caretModel.logicalPosition.column
    val textPrecedingCaret = CCEditorUtils.getCurrentLineUntilCaret(parameters.editor)
    val commitTokens = CCParser.parseText(textPrecedingCaret)
    val subjectCtx = isInSubjectContext(commitTokens)

    if (subjectCtx != null) {
      resultSet
        .withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
        .withRelevanceSorter(sorter(CommitSubjectElementWeigher))
        .also { rs ->
          safelyReleaseSemaphore(parameters.process)
          SUBJECT_EP.getExtensions(project)
            .asSequence()
            .sortedBy { config.getProviderOrder(it) }
            .flatMap {
              runWithCheckCanceled {
                it.getCommitSubjects(subjectCtx.first, subjectCtx.second)
              }.asSequence()
            }
            .map { CommitSubjectPsiElement(it, psiManager) }
            .mapIndexed { i, psi -> CommitSubjectLookupElement(i, psi) }
            .distinctBy { e -> e.lookupString }
            .forEach { rs.addElement(it) }
        }

      return
    }

    val scopeCtx = isInScopeContext(caretOffset - 1, commitTokens)

    if (scopeCtx != null) {
      val (type, scope) = scopeCtx
      resultSet
        .withPrefixMatcher(scope)
        .withRelevanceSorter(sorter(CommitScopeElementWeigher))
        .also { rs ->
          safelyReleaseSemaphore(parameters.process)
          SCOPE_EP.getExtensions(project)
            .asSequence()
            .sortedBy { config.getProviderOrder(it) }
            .flatMap { runWithCheckCanceled { it.getCommitScopes(type) }.asSequence() }
            .map { CommitScopePsiElement(it, psiManager) }
            .mapIndexed { i, psi -> CommitScopeLookupElement(i, psi) }
            .distinctBy { e -> e.lookupString }
            .forEach { rs.addElement(it) }
        }

      return
    }

    val typeStr = isInTypeContext(textPrecedingCaret, caretOffset, commitTokens.type)

    if (typeStr != null) {
      resultSet
        .withPrefixMatcher(typeStr)
        .withRelevanceSorter(sorter(CommitTypeElementWeigher))
        .also { rs ->
          safelyReleaseSemaphore(parameters.process)
          TYPE_EP.getExtensions(project)
            .asSequence()
            .sortedBy { config.getProviderOrder(it) }
            .flatMap { runWithCheckCanceled { it.getCommitTypes(typeStr) }.asSequence() }
            .map { CommitTypePsiElement(it, psiManager) }
            .mapIndexed { i, psi -> CommitTypeLookupElement(i, psi) }
            .distinctBy { e -> e.lookupString }
            .forEach { rs.addElement(it) }
        }
    }
  }

  private fun sorter(weigher: LookupElementWeigher): CompletionSorter {
    val sorter = CompletionSorter.emptySorter() as CompletionSorterImpl
    return sorter
      .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
      .withClassifier(CompletionSorterImpl.weighingFactory(weigher))
  }

  private fun isInTypeContext(text: String, caretOffset: Int, type: PCommitType): String? {
    return if (type.isInContext(caretOffset)) {
      type.value
    } else {
      text.trim()
    }
  }

  private fun isInScopeContext(caretOffset: Int, commitTokens: PCommitTokens): Pair<String, String>? =
    if (commitTokens.scope.isInContext(caretOffset)) {
      Pair(commitTokens.type.value, commitTokens.scope.value)
    } else {
      null
    }

  private fun isInSubjectContext(commitTokens: PCommitTokens): Pair<String, String>? =
    if (commitTokens.separator) {
      Pair(commitTokens.type.value, commitTokens.scope.value)
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
