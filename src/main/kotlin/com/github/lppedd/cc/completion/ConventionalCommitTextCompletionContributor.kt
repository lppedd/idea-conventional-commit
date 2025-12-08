@file:Suppress("UnstableApiUsage")

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
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupManagerListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.lang.LanguageMatcher
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.util.ReflectionUtil.findField
import com.intellij.util.concurrency.Semaphore
import java.lang.reflect.Field
import java.util.*
import java.util.Collections.newSetFromMap
import java.util.Collections.synchronizedMap
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides context-based completion items inside the VCS commit dialog.
 *
 * @author Edoardo Luppi
 */
internal class ConventionalCommitTextCompletionContributor : CompletionContributor(), DumbAware {
  @Suppress("CompanionObjectInExtension")
  private companion object {
    private val pattern = PlatformPatterns.psiElement().with(object : PatternCondition<PsiElement>(null) {
      override fun accepts(psiElement: PsiElement, context: ProcessingContext?): Boolean {
        val matcher = LanguageMatcher.matchWithDialects(PlainTextLanguage.INSTANCE)
        return matcher.matchesLanguage(psiElement.language)
      }
    })

    private val registeredProjects = newSetFromMap(ConcurrentHashMap<Project, Boolean>(16))
    private val lookupEnhancers = synchronizedMap(IdentityHashMap<Lookup, LookupEnhancer>(16))
  }

  private val myArrangerField: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    findField(CompletionProgressIndicator::class.java, null, "myArranger")
  }

  private val myFrozenItemsField: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    findField(CompletionLookupArrangerImpl::class.java, null, "myFrozenItems")
  }

  private val myFreezeSemaphoreField: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    findField(CompletionProgressIndicator::class.java, null, "myFreezeSemaphore")
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    // Only execute when inside the VCS commit dialog
    if (context.file.document?.isCommitMessage() == false) {
      return
    }

    val project = context.project

    if (registeredProjects.add(project)) {
      @Suppress("IncorrectParentDisposable")
      Disposer.register(project) {
        registeredProjects.remove(project)
      }

      project.messageBus
        .connect()
        .subscribe(LookupManagerListener.TOPIC, LookupCreationListener())
    }
  }

  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    if (parameters.completionType != BASIC || !pattern.accepts(parameters.position)) {
      return
    }

    val file = parameters.originalFile

    // Only execute when inside the VCS commit dialog
    if (file.document?.isCommitMessage() == false) {
      return
    }

    ProgressManager.checkCanceled()

    val project = file.project
    val resultSet = result
      .caseInsensitive()
      .withPrefixMatcher(FlatPrefixMatcher(parameters.getCompletionPrefix()))
      .withRelevanceSorter(
          CompletionSorter.emptySorter()
            .weigh(PreferStartMatching())
            .weigh(ConventionalCommitLookupElementWeigher(project.service()))
      )

    val editor = parameters.editor
    val isTemplateActive = editor.isTemplateActive()

    val myResultSet = if (isTemplateActive) {
      TemplateResultSet(resultSet)
    } else {
      ContextResultSet(resultSet)
    }

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
      // If we are completing a type with a template, we need to consider only
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
        null -> {
          // Not in a valid footer context
        }
      }
    }

    if (!isTemplateActive || !isInBodyOrFooterContext) {
      val commitTokens = CCParser.parseHeader(lineUntilCaret)

      when (val context = commitTokens.getContext(caretOffsetInLine)) {
        is TypeCommitContext -> {
          providers.add(TypeCompletionProvider(project, context))
        }
        is ScopeCommitContext -> {
          if (isTemplateActive) {
            providers.add(NoScopeCompletionProvider(project))
          }

          providers.add(ScopeCompletionProvider(project, context))
        }
        is SubjectCommitContext -> {
          providers.add(SubjectCompletionProvider(project, context))
        }
        null -> {
          // Not in a valid header context
        }
      }
    }

    if (providers.isNotEmpty()) {
      enhanceCompletionProcessIndicator(process, providers)
    }

    for (provider in providers) {
      ProgressManager.checkCanceled()
      provider.complete(myResultSet)

      if (provider.stopHere()) {
        break
      }
    }

    if (isTemplateActive) {
      myResultSet.stopHere()
    }
  }

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
        .flatMap(CompletionProvider<*>::getProviders)
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

  private class LookupCreationListener : LookupManagerListener {
    override fun activeLookupChanged(oldLookup: Lookup?, newLookup: Lookup?) {
      // isCompletion == true means the Lookup had already been created before and has been reused.
      // For our use case it means we've already installed the Lookup enhancer on that instance
      if (newLookup is LookupImpl && !isLookupEnhancerInstalled(newLookup)) {
        installLookupEnhancer(newLookup)
      }
    }

    fun isLookupEnhancerInstalled(lookup: LookupImpl): Boolean =
      lookupEnhancers.containsKey(lookup)

    fun installLookupEnhancer(lookup: LookupImpl) {
      check(lookupEnhancers.isEmpty()) { "Lookup enhancers map should be empty" }
      lookupEnhancers.computeIfAbsent(lookup) {
        Disposer.register(lookup) {
          lookupEnhancers.remove(lookup)
        }

        LookupEnhancer(lookup)
      }
    }
  }
}
