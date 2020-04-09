@file:Suppress("DEPRECATION", "UnstableApiUsage", "RedundantNotNullExtensionReceiverOfInline")

package com.github.lppedd.cc.completion

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.filter.MenuEnhancerLookupListener
import com.github.lppedd.cc.completion.providers.*
import com.github.lppedd.cc.completion.resultset.WrapperCompletionResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.noop.NoopList
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitContext.*
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.FooterContext.FooterValueContext
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ReflectionUtil.findField
import com.intellij.util.concurrency.Semaphore
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.Field
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.internal.InlineOnly

private val PLAIN_TEXT_PATTERN = PlatformPatterns.psiElement().withLanguage(PlainTextLanguage.INSTANCE)

/**
 * Provides context-based completion items inside the VCS commit dialog.
 *
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
private open class CommitCompletionContributor : CompletionContributor() {
  private val myArrangerField: Field by lazy(PUBLICATION) {
    findField(CompletionProgressIndicator::class.java, null, "myArranger")
  }

  private val myFrozenItemsField: Field by lazy(PUBLICATION) {
    findField(CompletionLookupArrangerImpl::class.java, null, "myFrozenItems")
  }

  private val myFreezeSemaphoreField: Field by lazy(PUBLICATION) {
    findField(CompletionProgressIndicator::class.java, null, "myFreezeSemaphore")
  }

  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    if (parameters.completionType != BASIC || !PLAIN_TEXT_PATTERN.accepts(parameters.position)) {
      return
    }

    val file = parameters.originalFile

    // Items must be provided only inside the VCS commit dialog's document
    if (file.document?.getUserData(CommitMessage.DATA_KEY) == null) {
      return
    }

    ProgressManager.checkCanceled()

    val project = file.project
    val configService = CCConfigService.getInstance(project)
    val resultSet = result
      .caseInsensitive()
      .withPrefixMatcher(FlatPrefixMatcher(parameters.getCompletionPrefix()))
      .withRelevanceSorter(sorter(CommitLookupElementWeigher(project)))

    val myResultSet = WrapperCompletionResultSet(resultSet)

    // If the user configured commit messages to be completed via templates,
    // we provide special `LookupElement`s which programmatically initiate
    // a `Template` instance on insertion
    if (configService.completionType == TEMPLATE) {
      if (!parameters.isAutoPopup) {
        ProgressManager.checkCanceled()
        TemplateTypeCompletionProvider(project, TypeCommitContext("")).complete(myResultSet)
      }

      return
    }

    val process = parameters.process
    val editor = parameters.editor
    val caretModel = editor.caretModel
    val (lineNumber, lineCaretOffset) = caretModel.logicalPosition
    val lineUntilCaret = editor.getCurrentLineUntilCaret()
    val providers = mutableListOf<CommitCompletionProvider<*>>()

    // After the second line we are inside the body/footer context
    if (lineNumber > 1) {
      val firstLineTokens = CCParser.parseHeader(editor.document.getLine(0))
      val footerTokens = CCParser.parseFooter(lineUntilCaret)

      when (val context = footerTokens.getContext(lineCaretOffset)) {
        is FooterTypeContext -> {
          providers.add(FooterTypeCompletionProvider(project, context))
          providers.add(BodyCompletionProvider(project, context, firstLineTokens))
        }
        is FooterValueContext -> {
          providers.add(FooterValueCompletionProvider(project, context, firstLineTokens, process))
        }
      }
    }

    val commitTokens = CCParser.parseHeader(lineUntilCaret)

    when (val context = commitTokens.getContext(lineCaretOffset)) {
      is TypeCommitContext -> providers.add(TypeCompletionProvider(project, context))
      is ScopeCommitContext -> providers.add(ScopeCompletionProvider(project, context))
      is SubjectCommitContext -> providers.add(SubjectCompletionProvider(project, context))
    }

    if (providers.isNotEmpty()) {
      if (process is CompletionProgressIndicator) {
        ProgressManager.checkCanceled()

        safelySetNoopListOnLookupArranger(process)
        safelyReleaseProcessSemaphore(process)

        val filterableProviders = providers.flatMap { it.providers.take(3) }.take(6)
        MenuEnhancerLookupListener(process.lookup).setProviders(filterableProviders)
      }
    }

    providers.forEach {
      ProgressManager.checkCanceled()
      it.complete(myResultSet)

      if (it.stopHere) {
        return
      }
    }
  }

  private fun sorter(weigher: LookupElementWeigher): CompletionSorter {
    return (CompletionSorter.emptySorter() as CompletionSorterImpl)
      .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
      .withClassifier(CompletionSorterImpl.weighingFactory(weigher))
  }

  /**
   * This is a workaround for a standard behavior which stores some elements
   * on the `CompletionLookupArrangerImpl#myFrozenItems` list.
   * Lookup elements are treated bunch by bunch, and those frozen items are kept
   * on the top of the popup `ListModel`. We don't want this, so we try to swap
   * the standard list with a no-op list, as in this way no elements get ever stored.
   */
  private fun safelySetNoopListOnLookupArranger(process: CompletionProgressIndicator) {
    try {
      @Suppress("UNCHECKED_CAST")
      process.getArranger().setFrozenItemsList(NoopList as MutableList<LookupElement?>)
    } catch (ignored: Exception) {
      // Let's just continue.
      // Though elements won't be ordered as we'd like
    }
  }

  /**
   * This is a workaround to a standard behavior which allows the UI
   * to freeze for about 2 seconds if the first `LookupElement` isn't
   * immediately added to the Lookup.
   * This standard behavior allows for auto-inserting a single element without
   * confusing the user by showing it, but as our `LookupElement`s never auto-complete,
   * we can get rid of it.
   */
  private fun safelyReleaseProcessSemaphore(process: CompletionProgressIndicator) {
    try {
      process.getFreezeSemaphore().up()
    } catch (ignored: Exception) {
      // Let's just continue
    }
  }

  @InlineOnly
  private inline fun CompletionProgressIndicator.getArranger(): CompletionLookupArrangerImpl =
    myArrangerField.get(this) as CompletionLookupArrangerImpl

  @InlineOnly
  private inline fun CompletionLookupArrangerImpl.setFrozenItemsList(list: MutableList<LookupElement?>) {
    myFrozenItemsField.set(this, list)
  }

  @InlineOnly
  private inline fun CompletionProgressIndicator.getFreezeSemaphore(): Semaphore =
    myFreezeSemaphoreField.get(this) as Semaphore
}
