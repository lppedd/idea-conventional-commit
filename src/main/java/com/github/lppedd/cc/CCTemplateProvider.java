package com.github.lppedd.cc;

import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;

public class CCTemplateProvider implements DefaultLiveTemplatesProvider {
  @Override
  public String[] getDefaultLiveTemplateFiles() {
    return new String[] {"liveTemplates/Conventional commit.xml"};
  }

  @Nullable
  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }
}
