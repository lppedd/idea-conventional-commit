package com.github.lppedd.cc

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Couple

internal fun <T> runWithCheckCanceled(callable: () -> T): T =
  ApplicationUtil.runWithCheckCanceled(callable, ProgressManager.getInstance().progressIndicator)

internal fun invokeLater(runnable: () -> Unit) {
  ApplicationManager.getApplication().invokeLater(runnable)
}

internal operator fun <T> Couple<T>.component1(): T? = first
internal operator fun <T> Couple<T>.component2(): T? = second
