package com.github.lppedd.cc.service.impl;

import static com.intellij.psi.search.FilenameIndex.getVirtualFilesByName;
import static com.intellij.psi.search.FilenameIndex.processAllFileNames;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

import com.github.lppedd.cc.CCIcons;
import com.github.lppedd.cc.service.CommitScopeHandler;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;

public class Angular2CommitScopeHandler implements CommitScopeHandler {
  private final Project project;

  Angular2CommitScopeHandler(final Project project) {
    this.project = project;
  }

  @Override
  public Collection<LookupElement> getScopes(final LookupImpl lookup) {
    final Collection<String> possibleModuleNames = new HashSet<>(32);
    final Processor<String> stringProcessor = fileName -> {
      if (fileName.toLowerCase().endsWith(".module.ts")) {
        possibleModuleNames.add(fileName);
      }

      return true;
    };

    final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);

    processAllFileNames(
        stringProcessor,
        projectScope,
        IdFilter.getProjectIdFilter(project, false)
    );

    return possibleModuleNames
        .stream()
        .flatMap(v -> getVirtualFilesByName(project, v, true, projectScope).stream())
        .map(v -> PsiUtilCore.getPsiFile(project, v))
        .filter(Angular2LangUtil::isAngular2Context)
        .map(PsiFileSystemItem::getName)
        .map(String::toLowerCase)
        .map(v -> v.replaceFirst(".module.ts$", ""))
        .map(Angular2LookupElement::new)
        .peek(v -> lookup.addItem(v, PrefixMatcher.ALWAYS_TRUE))
        .collect(Collectors.toList());
  }

  private static class Angular2LookupElement extends LookupElement {
    private final String text;

    Angular2LookupElement(final String text) {
      this.text = text;
    }

    @NotNull
    @Override
    public String getLookupString() {
      return text;
    }

    @Override
    public void renderElement(final LookupElementPresentation presentation) {
      presentation.setIcon(CCIcons.SCOPE);
      presentation.setItemText(text);
      presentation.setTypeText("NgModule", CCIcons.ANGULAR2);
      presentation.setTypeIconRightAligned(true);
    }

    @Override
    public boolean isCaseSensitive() {
      return false;
    }
  }
}
