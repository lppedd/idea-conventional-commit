package com.github.lppedd.cc.documentation;

import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.psi.CommitTypePsiElement;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;

public class CommitTypeDocumentationProvider extends AbstractDocumentationProvider {
  @Nullable
  @Override
  public String generateDoc(
      final PsiElement element,
      @Nullable final PsiElement originalElement) {
    if (!(element instanceof CommitTypePsiElement)) {
      return null;
    }

    final CommitTypePsiElement commitType = (CommitTypePsiElement) element;
    return commitType.getCommitTypeDescription();
  }
}
