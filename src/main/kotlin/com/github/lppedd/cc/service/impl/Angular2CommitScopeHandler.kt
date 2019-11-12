package com.github.lppedd.cc.service.impl

import com.github.lppedd.cc.ConventionalCommitIcons
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import org.angular2.lang.Angular2LangUtil
import java.util.*

/**
 * @author Edoardo Luppi
 */
class Angular2CommitScopeHandler(private val project: Project) {
  fun getScopes(lookup: LookupImpl): Collection<LookupElement> {
    val possibleModuleNames: MutableCollection<String> = HashSet(32)
    val stringProcessor = Processor { fileName: String ->
      if (fileName.toLowerCase().endsWith(".module.ts")) {
        possibleModuleNames.add(fileName)
      }
      true
    }

    val projectScope = GlobalSearchScope.projectScope(project)
    FilenameIndex.processAllFileNames(
      stringProcessor,
      projectScope,
      IdFilter.getProjectIdFilter(project, false)
    )

    return possibleModuleNames
      .flatMap { v -> FilenameIndex.getVirtualFilesByName(project, v, true, projectScope) }
      .asSequence()
      .map { v -> PsiUtilCore.getPsiFile(project, v!!) }
      .filter { context -> Angular2LangUtil.isAngular2Context(context) }
      .map { obj -> obj.name }
      .map { obj -> obj.toLowerCase() }
      .map { v -> v.replaceFirst(".module.ts$".toRegex(), "") }
      .map { text: String -> Angular2LookupElement(text) }
      .onEach { v -> lookup.addItem(v, PrefixMatcher.ALWAYS_TRUE) }
      .toList()
  }

  private class Angular2LookupElement(private val text: String) : LookupElement() {
    override fun getLookupString() = text

    override fun renderElement(presentation: LookupElementPresentation) {
      presentation.icon = ConventionalCommitIcons.SCOPE
      presentation.itemText = text
      presentation.setTypeText("NgModule", ConventionalCommitIcons.ANGULAR2)
      presentation.isTypeIconRightAligned = true
    }

    override fun isCaseSensitive() = false
  }
}
