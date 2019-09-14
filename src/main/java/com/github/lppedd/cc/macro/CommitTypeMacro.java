package com.github.lppedd.cc.macro;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.CCBundle;
import com.github.lppedd.cc.lookup.CommitTypeLookupElement;
import com.github.lppedd.cc.psi.CommitTypePsiElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.psi.PsiManager;

public class CommitTypeMacro extends Macro {
  private static final String NAME = "conventionalCommitType";

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
    final PsiManager psiManager = PsiManager.getInstance(context.getProject());
    return new LookupElement[] {
        // @formatter:off
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "refactor", CCBundle.message("commit.type.refactor"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "fix", CCBundle.message("commit.type.fix"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "feat", CCBundle.message("commit.type.feat"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "build", CCBundle.message("commit.type.build"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "style", CCBundle.message("commit.type.style"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "test", CCBundle.message("commit.type.test"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "docs", CCBundle.message("commit.type.docs"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "perf", CCBundle.message("commit.type.perf"))),
        new CommitTypeLookupElement(new CommitTypePsiElement(psiManager, "ci", CCBundle.message("commit.type.ci")))
        // @formatter:on
    };
  }
}
