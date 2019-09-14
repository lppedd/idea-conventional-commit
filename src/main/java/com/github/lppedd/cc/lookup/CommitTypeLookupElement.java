package com.github.lppedd.cc.lookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.CCIcons;
import com.github.lppedd.cc.psi.CommitTypePsiElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiElement;

public class CommitTypeLookupElement extends LookupElement {
  private final CommitTypePsiElement psiElement;

  public CommitTypeLookupElement(final CommitTypePsiElement psiElement) {
    this.psiElement = psiElement;
  }

  @Override
  public void renderElement(final LookupElementPresentation presentation) {
    presentation.setItemText(psiElement.getCommitTypeName());
    presentation.setIcon(CCIcons.TYPE);
  }

  @Nullable
  @Override
  public PsiElement getPsiElement() {
    return psiElement;
  }

  @NotNull
  @Override
  public String getLookupString() {
    return psiElement.getCommitTypeName();
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }
}
