package com.github.lppedd.cc.api

import com.github.lppedd.cc.inspection.CommitBaseInspection
import com.intellij.openapi.extensions.ExtensionPointName

internal val INSPECTION_EP = ExtensionPointName<CommitInspectionProvider>(
  "com.github.lppedd.idea-conventional-commit.commitInspectionProvider"
)

/**
 * @author Edoardo Luppi
 */
interface CommitInspectionProvider {
  fun getInspections(): Collection<CommitBaseInspection>
}
