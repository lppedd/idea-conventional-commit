package com.github.lppedd.cc.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.Language;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

public class CommitTypePsiElement extends LightElement {
  private final String commitTypeName;
  private final String commitTypeDescription;

  public CommitTypePsiElement(
      @NotNull final PsiManager psiManager,
      @NotNull final String commitTypeName,
      @Nullable final String commitTypeDescription) {
    super(psiManager, Language.ANY);
    this.commitTypeName = commitTypeName;
    this.commitTypeDescription = commitTypeDescription;
  }

  @NotNull
  public String getCommitTypeName() {
    return commitTypeName;
  }

  @Nullable
  public String getCommitTypeDescription() {
    return commitTypeDescription;
  }

  @Override
  public String toString() {
    return "Commit type - " + commitTypeName;
  }
}
