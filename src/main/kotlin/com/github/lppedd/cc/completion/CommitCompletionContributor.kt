@file:Suppress("UnstableApiUsage", "deprecation")

package com.github.lppedd.cc.completion

import com.github.lppedd.cc.*
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.collection.NoopList
import com.github.lppedd.cc.completion.providers.*
import com.github.lppedd.cc.completion.providers.CompletionProvider
import com.github.lppedd.cc.completion.resultset.ContextResultSet
import com.github.lppedd.cc.completion.resultset.TemplateResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType.TEMPLATE
import com.github.lppedd.cc.lookupElement.INDEX_TYPE
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitContext.*
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.FooterContext.FooterValueContext
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Disposer
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ReflectionUtil.findField
import com.intellij.util.concurrency.Semaphore
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.lang.reflect.Field
import java.util.*
import java.util.Collections.synchronizedMap
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * Provides context-based completion items inside the VCS commit dialog.
 *
 * @author Edoardo Luppi
 */
private class CommitCompletionContributor : CompletionContributor() {
  private companion object {
    private val plainTextPattern = PlatformPatterns.psiElement().withLanguage(PlainTextLanguage.INSTANCE)
    private val lookupEnhancers = synchronizedMap(IdentityHashMap<Lookup, LookupEnhancer>(16))
  }

  private val myArrangerField: Field by lazy(PUBLICATION) {
    findField(CompletionProgressIndicator::class.java, null, "myArranger")
  }

  private val myFrozenItemsField: Field by lazy(PUBLICATION) {
    findField(CompletionLookupArrangerImpl::class.java, null, "myFrozenItems")
  }

  private val myFreezeSemaphoreField: Field by lazy(PUBLICATION) {
    findField(CompletionProgressIndicator::class.java, null, "myFreezeSemaphore")
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    // Only execute when inside the VCS commit dialog
    if (context.file.document?.isCommitMessage() == false) {
      return
    }

    val lookupManager = LookupManager.getInstance(context.project)

    // isCompletion == true means the Lookup had already been created before
    // and has now been reused.
    // For our use case it means we've already added the Lookup creation listener
    // and installed the Lookup enhancer on that Lookup instance
    if (lookupManager.activeLookup?.isCompletion != true) {
      lookupManager.addPropertyChangeListener(LookupCreationListener(lookupManager))
    }
  }

  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    if (parameters.completionType != BASIC || !plainTextPattern.accepts(parameters.position)) {
      return
    }

    val file = parameters.originalFile

    // Only execute when inside the VCS commit dialog
    if (file.document?.isCommitMessage() == false) {
      return
    }

    ProgressManager.checkCanceled()

    val resultSet = result
      .caseInsensitive()
      .withPrefixMatcher(FlatPrefixMatcher(parameters.getCompletionPrefix()))
      .withRelevanceSorter(sorter(CommitLookupElementWeigher))

    val editor = parameters.editor
    val isTemplateActive = editor.isTemplateActive()

    val myResultSet = if (isTemplateActive) {
      TemplateResultSet(resultSet)
    } else {
      ContextResultSet(resultSet)
    }

    val project = file.project
    val configService = project.service<CCConfigService>()
    val process = parameters.process

    // If the user configured commit messages to be completed via templates,
    // we provide special `LookupElement`s which programmatically initiate
    // a `Template` instance on insertion
    if (configService.completionType == TEMPLATE && !isTemplateActive) {
      if (!parameters.isAutoPopup) {
        val provider = TemplateTypeCompletionProvider(project, TypeCommitContext(""))
        enhanceCompletionProcessIndicator(process, listOf(provider))
        provider.complete(myResultSet)
      }

      return
    }

    val document = editor.document
    val caretLogicalPosition = editor.caretModel.logicalPosition
    val caretLineNumber = caretLogicalPosition.line
    var caretOffsetInLine = caretLogicalPosition.column
    val templateState = editor.getTemplateState()
    val lineUntilCaret = if (templateState?.currentVariableNumber == INDEX_TYPE) {
      // If we are completing a type with template, we need to consider only
      // the part of the line after the range marker's start
      val typeStartOffset = templateState.getSegmentRange(INDEX_TYPE).startOffset
      val start = typeStartOffset - document.getLineRangeByOffset(typeStartOffset).startOffset

      caretOffsetInLine -= typeStartOffset
      document.getSegment(start, start + caretOffsetInLine)
    } else {
      val lineStartOffset = document.getLineStartOffset(caretLineNumber)
      document.getSegment(lineStartOffset, lineStartOffset + caretOffsetInLine)
    }

    val providers = mutableListOf<CompletionProvider<*>>()

    // After the second line we are inside the body/footer context
    val isInBodyOrFooterContext = caretLineNumber > 1

    if (isInBodyOrFooterContext) {
      val firstLineTokens = CCParser.parseHeader(document.getLine(0))
      val footerTokens = CCParser.parseFooter(lineUntilCaret)

      when (val context = footerTokens.getContext(caretOffsetInLine)) {
        is FooterTypeContext -> {
          providers.add(FooterTypeCompletionProvider(project, context))
          providers.add(BodyCompletionProvider(project, context, firstLineTokens))
        }
        is FooterValueContext -> {
          providers.add(FooterValueCompletionProvider(project, context, firstLineTokens, process))
        }
      }
    }

    if (!isTemplateActive || !isInBodyOrFooterContext) {
      val commitTokens = CCParser.parseHeader(lineUntilCaret)

      when (val context = commitTokens.getContext(caretOffsetInLine)) {
        is TypeCommitContext -> providers.add(TypeCompletionProvider(project, context))
        is ScopeCommitContext -> {
          if (isTemplateActive) {
            providers.add(NoScopeCompletionProvider(project))
          }

          providers.add(ScopeCompletionProvider(project, context))
        }
        is SubjectCommitContext -> providers.add(SubjectCompletionProvider(project, context))
      }
    }

    if (providers.isNotEmpty()) {
      enhanceCompletionProcessIndicator(process, providers)
    }

    for (provider in providers) {
      ProgressManager.checkCanceled()
      provider.complete(myResultSet)

      if (provider.stopHere) {
        break
      }
    }

    if (isTemplateActive) {
      myResultSet.stopHere()
    }
  }

  private fun sorter(weigher: LookupElementWeigher): CompletionSorter =
    (CompletionSorter.emptySorter() as CompletionSorterImpl)
      .withClassifier(CompletionSorterImpl.weighingFactory(PreferStartMatching()))
      .withClassifier(CompletionSorterImpl.weighingFactory(weigher))

  private fun enhanceCompletionProcessIndicator(
      process: CompletionProcess,
      completionProviders: Collection<CompletionProvider<*>>,
  ) {
    if (process !is CompletionProgressIndicator) {
      return
    }

    ProgressManager.checkCanceled()
    safelySetNoopListOnLookupArranger(process)
    safelyReleaseProcessSemaphore(process)

    // Only token providers that will be executed need to be filterable,
    // so take them until the one that says "stopHere"
    var n = completionProviders.indexOfFirst(CompletionProvider<*>::stopHere) + 1

    if (n == 0) {
      // All the token providers will be executed and thus
      // all need to be filterable
      n = completionProviders.size
    }

    val commitTokenProviders =
      completionProviders
        .asSequence()
        .take(n)
        .flatMap(CompletionProvider<*>::providers)
        // Removing duplicated token providers avoids having duplicated
        // filtering actions in the menu
        .distinctBy(CommitTokenProvider::getId)
        .toList()

    checkNotNull(lookupEnhancers[process.lookup]).setProviders(commitTokenProviders)
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
      @Suppress("unchecked_cast")
      process.getArranger().setFrozenItemsList(NoopList as MutableList<LookupElement?>)
    } catch (_: Exception) {
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
    } catch (_: Exception) {
      // Let's just continue
    }
  }

  private fun CompletionProgressIndicator.getArranger(): CompletionLookupArrangerImpl =
    myArrangerField.get(this) as CompletionLookupArrangerImpl

  private fun CompletionLookupArrangerImpl.setFrozenItemsList(list: MutableList<LookupElement?>) {
    myFrozenItemsField.set(this, list)
  }

  private fun CompletionProgressIndicator.getFreezeSemaphore(): Semaphore =
    myFreezeSemaphoreField.get(this) as Semaphore

  private class LookupCreationListener(val lookupManager: LookupManager) : PropertyChangeListener {
    override fun propertyChange(event: PropertyChangeEvent) {
      if (event.propertyName == LookupManager.PROP_ACTIVE_LOOKUP) {
        when (val lookup = event.newValue) {
          is LookupImpl -> installLookupEnhancer(lookup)

          // The Lookup has been disposed, thus we can remove ourselves from the listeners
          else -> lookupManager.removePropertyChangeListener(this)
        }
      }
    }

    fun installLookupEnhancer(lookup: LookupImpl) {
      lookupEnhancers.computeIfAbsent(lookup) {
        Disposer.register(lookup) { lookupEnhancers.remove(lookup) }
        LookupEnhancer(lookup)
      }
    }
  }
}
