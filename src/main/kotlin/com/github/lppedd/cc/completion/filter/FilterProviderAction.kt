package com.github.lppedd.cc.completion.filter

import com.github.lppedd.cc.ICON_DISABLED
import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class FilterProviderAction(
    private val lookup: LookupImpl,
    private val provider: CommitTokenProvider,
) : AnAction("Provider: ${provider.getPresentation().name}") {
  private var isFiltered = false
  private val providerId = provider.getId()
  private var backupItems = emptyList<CommitLookupElement>()

  fun reset() {
    if (isFiltered) {
      performAction()
    }
  }

  override fun actionPerformed(ignored: AnActionEvent) {
    performAction()
  }

  override fun update(e: AnActionEvent) {
    e.presentation.icon = if (isFiltered) {
      ICON_DISABLED
    } else {
      provider.getPresentation().icon
    }
  }

  private fun performAction() {
    isFiltered = !isFiltered

    if (isFiltered) {
      filterLookupElements()
    } else {
      reinstallFilteredLookupElements()
    }

    lookup.resort(false)
  }

  private fun filterLookupElements() {
    val arranger = lookup.arranger

    backupItems = lookup.items
      .asSequence()
      .filterIsInstance<CommitLookupElement>()
      .filter { providerId == it.provider.getId() }
      .onEach {
        val delegatePrefixMatcher = arranger.itemMatcher(it)
        val newPrefixMatcher = FilterPrefixMatcher(delegatePrefixMatcher)
        arranger.registerMatcher(it, newPrefixMatcher)
        it.valid = false
      }.toList()
  }

  private fun reinstallFilteredLookupElements() {
    val arranger = lookup.arranger

    backupItems.firstOrNull()?.let {
      arranger.prefixReplaced(lookup, arranger.itemMatcher(it).prefix)
    }

    backupItems.forEach {
      it.valid = true
      val delegate = arranger.itemMatcher(it)
      val newPrefixMatcher = PlainPrefixMatcher(delegate.prefix)
      arranger.registerMatcher(it, newPrefixMatcher)
      lookup.addItem(it, newPrefixMatcher)
    }

    backupItems = emptyList()
  }
}
