package com.github.lppedd.cc.lookup;

import org.jetbrains.annotations.NotNull;

import com.github.lppedd.cc.CCIcons;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;

public class CommitDescriptionLookupElement extends LookupElement {
  private final String description;

  public CommitDescriptionLookupElement(final String description) {
    this.description = description;
  }

  @Override
  public void renderElement(final LookupElementPresentation presentation) {
    presentation.setItemText(description);
    presentation.setIcon(CCIcons.DESCRIPTION);
  }

  @NotNull
  @Override
  public String getLookupString() {
    return description;
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }
}
