// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.lppedd.cc;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import com.intellij.CommonBundle;

public final class CCBundle {
  @NonNls
  private static final String BUNDLE = "messages.ConventionalCommitBundle";
  private static Reference<ResourceBundle> bundleReference;

  public static String message(
      @NotNull @PropertyKey(resourceBundle = BUNDLE) final String key,
      @NotNull final Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    // noinspection StaticVariableUsedBeforeInitialization
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(bundleReference);

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      bundleReference = new SoftReference<>(bundle);
    }

    return bundle;
  }
}
