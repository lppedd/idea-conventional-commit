package com.github.lppedd.cc.completion

import com.github.lppedd.cc.api.SCOPE_EP
import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.api.TYPE_EP
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.document
import com.github.lppedd.cc.getCompletionPrefix
import com.github.lppedd.cc.getCurrentLineUntilCaret
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement
import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement
import com.github.lppedd.cc.lookupElement.TemplateCommitTypeLookupElement
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.Context.*
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.intellij.util.concurrency.Semaphore

/**
 * Provides context-based completion items inside the VCS commit dialog.
 *
 * @author Edoardo Luppi
 */
private class CommitCompletionContributor : CompletionContributor() {
  init {
    extend(
      CompletionType.BASIC,
      PlatformPatterns.psiElement().withLanguage(PlainTextLanguage.INSTANCE),
      CommitCompletionProvider
    )
  }

  object CommitCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        processingContext: ProcessingContext,
        result: CompletionResultSet,
    ) {
      val file = parameters.originalFile

      // Items must be provided only inside the VCS commit dialog,
      // not in all generic text documents
      if (file.document?.getUserData(CommitMessage.DATA_KEY) == null) {
        return
      }

      val project = file.project
      val configService = CCConfigService.getInstance(project)
      val resultSet = result
        .caseInsensitive()
        .withPrefixMatcher(PlainPrefixMatcher(parameters.getCompletionPrefix()))

      // If the user configured commit messages to be completed via templates,
      // we provide special `LookupElement`s which programmatically initiate
      // a `Template` instance on insertion
      if (configService.completionType === TEMPLATE) {
        if (!parameters.isAutoPopup) {
          val rs = resultSet.withRelevanceSorter(sorter(CommitLookupElementWeigher))
          TYPE_EP.getExtensions(project)
            .asSequence()
            .sortedBy(configService::getProviderOrder)
            .flatMap { runWithCheckCanceled { it.getCommitTypes("") }.asSequence() }
            .map { commitType -> CommitTypePsiElement(project, commitType) }
            .mapIndexed(::TemplateCommitTypeLookupElement)
            .distinctBy(TemplateCommitTypeLookupElement::getLookupString)
            .forEach(rs::addElement)
        }

        return
      }

      fun fillResultSetWithTypes(context: TypeContext) {
        val rs = resultSet
          .withPrefixMatcher(context.type)
          .withRelevanceSorter(sorter(CommitLookupElementWeigher))

        safelyReleaseSemaphore(parameters.process)
        TYPE_EP.getExtensions(project)
          .asSequence()
          .sortedBy(configService::getProviderOrder)
          .flatMap { runWithCheckCanceled { it.getCommitTypes(context.type) }.asSequence() }
          .map { CommitTypePsiElement(project, it) }
          .mapIndexed(::CommitTypeLookupElement)
          .distinctBy(CommitTypeLookupElement::getLookupString)
          .forEach(rs::addElement)
      }

      fun fillResultSetWithScopes(context: ScopeContext) {
        val rs = resultSet
          .withPrefixMatcher(context.scope.trimStart())
          .withRelevanceSorter(sorter(CommitLookupElementWeigher))

        safelyReleaseSemaphore(parameters.process)
        SCOPE_EP.getExtensions(project)
          .asSequence()
          .sortedBy(configService::getProviderOrder)
          .flatMap { runWithCheckCanceled { it.getCommitScopes(context.type) }.asSequence() }
          .map { CommitScopePsiElement(project, it) }
          .mapIndexed(::CommitScopeLookupElement)
          .distinctBy(CommitScopeLookupElement::getLookupString)
          .forEach(rs::addElement)
      }

      fun fillResultSetWithSubjects(context: SubjectContext) {
        val rs = resultSet
          .withPrefixMatcher(context.subject.trimStart())
          .withRelevanceSorter(sorter(CommitLookupElementWeigher))

        safelyReleaseSemaphore(parameters.process)
        SUBJECT_EP.getExtensions(project)
          .asSequence()
          .sortedBy(configService::getProviderOrder)
          .flatMap {
            runWithCheckCanceled {
              it.getCommitSubjects(context.type, context.scope)
            }.asSequence()
          }
          .map { CommitSubjectPsiElement(project, it) }
          .mapIndexed(::CommitSubjectLookupElement)
          .distinctBy(CommitSubjectLookupElement::getLookupString)
          .forEach(rs::addElement)
      }

      val editor = parameters.editor
      val caretOffset = editor.caretModel.logicalPosition.column
      val textUntilCaret = editor.getCurrentLineUntilCaret()
      val commitTokens = CCParser.parseText(textUntilCaret)

      when (val context = commitTokens.getContext(caretOffset)) {
        is TypeContext -> fillResultSetWithTypes(context)
        is ScopeContext -> fillResultSetWithScopes(context)
        is SubjectContext -> fillResultSetWithSubjects(context)
      }
    }

    private fun sorter(weigher: LookupElementWeigher): CompletionSorter {
      return (CompletionSorter.emptySorter() as CompletionSorterImpl)
        .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
        .withClassifier(CompletionSorterImpl.weighingFactory(weigher))
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
          process.getFreezeSemaphore().up()
        } catch (ignored: Exception) {
          // Let's just continue
        }
      }
    }

    @Suppress("DEPRECATION")
    private fun CompletionProgressIndicator.getFreezeSemaphore(): Semaphore =
      javaClass.getDeclaredField("myFreezeSemaphore").let {
        it.isAccessible = true
        it.get(this) as Semaphore
      }
  }
}
