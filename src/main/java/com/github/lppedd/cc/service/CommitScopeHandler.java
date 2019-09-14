package com.github.lppedd.cc.service;

import java.util.Collection;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.impl.LookupImpl;

public interface CommitScopeHandler {
  @SuppressWarnings("UnusedReturnValue")
  Collection<LookupElement> getScopes(final LookupImpl lookup);
}
