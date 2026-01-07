package com.github.lppedd.cc.completion.menu

import com.github.lppedd.cc.api.CommitTokenProvider
import com.github.lppedd.cc.completion.FilterPrefixMatcher
import com.github.lppedd.cc.completion.LookupElementKey
import com.github.lppedd.cc.completion.LookupEnhancer
import com.github.lppedd.cc.lookupElement.CommitTokenLookupElement
import com.github.lppedd.cc.updateIcons
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.util.*

/**
 * @author Edoardo Luppi
 */
internal class FilterAction(
  private val enhancer: LookupEnhancer,
  private val lookup: LookupImpl,
  private val provider: CommitTokenProvider,
) : AnAction(provider.getPresentation().getName()) {
  private var isFiltered = false
  private var backupItems = emptyList<CommitTokenLookupElement>()

  fun filterItems(doFilter: Boolean) {
    if (isFiltered != doFilter) {
      isFiltered = doFilter

      if (doFilter) {
        removeLookupItems()
      } else {
        reinstallLookupItems()
      }
    }
  }

  private fun removeLookupItems() {
    val arranger = lookup.arranger

    backupItems = lookup.items
      .asSequence()
      .filterIsInstance<CommitTokenLookupElement>()
      .filter {
        val elementProvider = it.getUserData(LookupElementKey.Provider) ?: error("missing element provider")
        provider.getId() == elementProvider.getId()
      }.onEach {
        val delegatePrefixMatcher = arranger.itemMatcher(it)
        val newPrefixMatcher = FilterPrefixMatcher(delegatePrefixMatcher)
        arranger.registerMatcher(it, newPrefixMatcher)
        it.setVisible(false)
      }.toList()
  }

  private fun reinstallLookupItems() {
    val arranger = lookup.arranger

    arranger.matchingItems.firstOrNull()?.let {
      arranger.prefixReplaced(lookup, arranger.itemMatcher(it).prefix)
    }

    backupItems.forEach {
      it.setVisible(true)
      val prefix = arranger.itemMatcher(it).prefix
      val newPrefixMatcher = PlainPrefixMatcher(prefix)
      lookup.addItem(it, newPrefixMatcher)
    }

    backupItems = emptyList()
  }

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.EDT

  override fun actionPerformed(event: AnActionEvent) {
    if (enhancer.filterSelected(this)) {
      filterItems(!isFiltered)
      lookup.resort(false)
    }
  }

  override fun update(event: AnActionEvent) {
    val presentation = provider.getPresentation()
    val icon = if (isFiltered) {
      presentation.getDisabledIcon()
    } else {
      presentation.getIcon()
    }

    event.presentation.updateIcons(icon)
  }

  override fun equals(other: Any?): Boolean =
    other is FilterAction && provider.getId() == other.provider.getId()

  override fun hashCode(): Int =
    Objects.hashCode(provider.getId())
}
