package com.github.lppedd.cc.api

import com.github.lppedd.cc.inspection.CommitBaseInspection
import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.annotations.ApiStatus

internal val INSPECTION_EP = ExtensionPointName<CommitInspectionProvider>(
  "com.github.lppedd.idea-conventional-commit.commitInspectionProvider"
)

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince("0.10.0")
interface CommitInspectionProvider {
  fun getInspections(): Collection<CommitBaseInspection>
}
