package com.github.lppedd.cc.macro;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.lookup.CommitDescriptionLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsConfiguration;

public class CommitDescriptionMacro extends Macro {
  private static final String NAME = "conventionalCommitDescription";

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
    return null;
  }

  @Nullable
  @Override
  public LookupElement[] calculateLookupItems(
      @NotNull final Expression[] params,
      @Nullable final ExpressionContext context) {
    return getRecentVcsMessages(context.getProject(), 20);
  }

  @SuppressWarnings("SameParameterValue")
  private static LookupElement[] getRecentVcsMessages(
      @NotNull final Project project,
      final int limit) {
    final List<String> recentMessages = VcsConfiguration.getInstance(project).getRecentMessages();
    Collections.reverse(recentMessages);
    return recentMessages
        .stream()
        .limit(limit)
        .map(v -> v.replaceFirst("(^(build|fix|refactor|chore|feat|docs)).*:", ""))
        .map(String::trim)
        .map(CommitDescriptionLookupElement::new)
        .toArray(LookupElement[]::new);
  }
}
