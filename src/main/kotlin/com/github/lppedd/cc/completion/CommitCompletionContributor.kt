package com.github.lppedd.cc.completion

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.lookupElement.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitContext.*
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.FooterContext.FooterValueContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.openapi.editor.Editor
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
        .withRelevanceSorter(sorter(CommitLookupElementWeigher))

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

      fun fillResultSetWithTypes(context: TypeCommitContext) {
        val rs = resultSet.withPrefixMatcher(context.type)
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

      fun fillResultSetWithScopes(context: ScopeCommitContext) {
        val rs = resultSet.withPrefixMatcher(context.scope.trimStart())
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

      fun fillResultSetWithSubjects(context: SubjectCommitContext) {
        val rs = resultSet.withPrefixMatcher(context.subject.trimStart())
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
      val lineUntilCaret = editor.getCurrentLineUntilCaret()
      val commitTokens = CCParser.parseHeader(lineUntilCaret)

      if (isInBodyOrFooterContext(editor)) {
        val footerEps =
          FOOTER_EP.getExtensions(project)
            .asSequence()
            .sortedBy(configService::getProviderOrder)

        val (type, scope, _, _, subject) = CCParser.parseHeader(editor.document.getLine(0))

        fun fillResultSetWithBodiesAndFooterTypes(context: FooterTypeContext) {
          val rs = resultSet.withPrefixMatcher(FlatPrefixMatcher(context.type))
          safelyReleaseSemaphore(parameters.process)

          footerEps.flatMap {
              runWithCheckCanceled(it::getCommitFooterTypes).asSequence()
            }
            .map { CommitFooterTypePsiElement(project, it) }
            .mapIndexed(::CommitFooterTypeLookupElement)
            .distinctBy(CommitLookupElement::getLookupString)
            .forEach(rs::addElement)

          BODY_EP.getExtensions(project)
            .asSequence()
            .sortedBy(configService::getProviderOrder)
            .flatMap {
              runWithCheckCanceled {
                it.getCommitBodies(
                  (type as? ValidToken)?.value,
                  (scope as? ValidToken)?.value,
                  (subject as? ValidToken)?.value
                )
              }.asSequence()
            }
            .map { CommitBodyPsiElement(project, it) }
            .mapIndexed { i, psi -> CommitBodyLookupElement(i, psi, context.type) }
            .distinctBy(CommitLookupElement::getLookupString)
            .forEach(rs::addElement)
        }

        fun buildShowMoreLookupElement(prefix: String): CommitLookupElement {
          val commitFooter = CommitFooter("", CCBundle["cc.config.coAuthors.description"])
          val psiElement = CommitFooterPsiElement(project, commitFooter)
          val lookupElement = ShowMoreCoAuthorsLookupElement(2000, psiElement, prefix)
          val process = parameters.process

          @Suppress("DEPRECATION")
          if (process is CompletionProgressIndicator) {
            process.lookup.addPrefixChangeListener(lookupElement, process)
          }

          return lookupElement
        }

        fun fillResultSetWithFooterValues(context: FooterValueContext) {
          val prefix = context.value.trimStart()
          val rs = resultSet.withPrefixMatcher(FlatPrefixMatcher(prefix))
          safelyReleaseSemaphore(parameters.process)

          footerEps.flatMap {
              runWithCheckCanceled {
                it.getCommitFooters(
                  context.type,
                  (type as? ValidToken)?.value,
                  (scope as? ValidToken)?.value,
                  (subject as? ValidToken)?.value
                )
              }.asSequence()
            }
            .map { CommitFooterPsiElement(project, it) }
            .mapIndexed { i, psi -> CommitFooterLookupElement(i, psi, prefix) }
            .distinctBy(CommitLookupElement::getLookupString)
            .forEach(rs::addElement)

          if ("co-authored-by".equals(context.type, true)) {
            rs.addElement(buildShowMoreLookupElement(prefix))
          }

          rs.stopHere()
        }

        val footerTokens = CCParser.parseFooter(lineUntilCaret)

        when (val context = footerTokens.getContext(caretOffset)) {
          is FooterTypeContext -> fillResultSetWithBodiesAndFooterTypes(context)
          is FooterValueContext -> return fillResultSetWithFooterValues(context)
        }
      }

      when (val context = commitTokens.getContext(caretOffset)) {
        is TypeCommitContext -> fillResultSetWithTypes(context)
        is ScopeCommitContext -> fillResultSetWithScopes(context)
        is SubjectCommitContext -> fillResultSetWithSubjects(context)
      }
    }

    private fun sorter(weigher: LookupElementWeigher): CompletionSorter {
      return (CompletionSorter.emptySorter() as CompletionSorterImpl)
        .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
        .withClassifier(CompletionSorterImpl.weighingFactory(weigher))
    }

    private fun isInBodyOrFooterContext(editor: Editor): Boolean =
      editor.caretModel.logicalPosition.line > 1

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
  }
}

@Suppress("DEPRECATION")
private fun CompletionProgressIndicator.getFreezeSemaphore(): Semaphore =
  javaClass.getDeclaredField("myFreezeSemaphore").let {
    it.isAccessible = true
    it.get(this) as Semaphore
  }
