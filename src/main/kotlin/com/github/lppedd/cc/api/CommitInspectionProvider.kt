package com.github.lppedd.cc.api

import com.github.lppedd.cc.inspection.CommitBaseInspection
import com.intellij.openapi.extensions.ProjectExtensionPointName
import org.jetbrains.annotations.ApiStatus.*

@JvmSynthetic
internal val INSPECTION_EP = ProjectExtensionPointName<CommitInspectionProvider>(
  "com.github.lppedd.idea-conventional-commit.commitInspectionProvider"
)

/**
 * @author Edoardo Luppi
 */
@Experimental
@AvailableSince("0.10.0")
interface CommitInspectionProvider {
  fun getInspections(): Collection<CommitBaseInspection>
}
