package com.github.lppedd.cc.macro;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.service.CommitScopeHandler;
import com.intellij.codeInsight.lookup.LookupArranger.DefaultArranger;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.InvokeActionResult;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class CommitScopeMacro extends Macro {
  private static final String NAME = "conventionalCommitScope";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getPresentableName() {
    return NAME + "()";
  }

  @Nullable
  @Override
  public Result calculateResult(
      @NotNull final Expression[] params,
      @Nullable final ExpressionContext context) {
    return new InvokeActionResult(() -> invokeCompletion(context));
  }

  private void invokeCompletion(final ExpressionContext context) {
    final Project project = context.getProject();
    final Editor editor = context.getEditor();
    final LookupManager lookupManager = LookupManager.getInstance(project);

    final Runnable runnable = () -> {
      final LookupImpl lookup = (LookupImpl) lookupManager.createLookup(
          editor,
          LookupElement.EMPTY_ARRAY,
          "",
          new DefaultArranger()
      );

      lookup.setCalculating(true);
      findNgModules(project, lookup);
      lookup.setCalculating(false);
      lookup.showLookup();
      lookup.refreshUi(true, true);
      lookup.ensureSelectionVisible(true);
    };

    ApplicationManager.getApplication().invokeLater(runnable);
  }

  private static void findNgModules(
      @NotNull final Project project,
      @NotNull final LookupImpl lookup) {
    final CommitScopeHandler scopeHandler =
        ServiceManager.getService(project, CommitScopeHandler.class);

    if (scopeHandler != null) {
      scopeHandler.getScopes(lookup);
    }
  }
}
