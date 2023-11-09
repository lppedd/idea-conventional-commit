package com.github.lppedd.cc.api.impl

import com.github.lppedd.cc.api.CommitInspectionProvider
import com.github.lppedd.cc.api.CommitInspectionProviderService
import com.intellij.openapi.extensions.ExtensionPointName

/**
 * @author Edoardo Luppi
 */
internal class InternalCommitInspectionProviderService : CommitInspectionProviderService {
  private companion object {
    private val inspectionEpName: ExtensionPointName<CommitInspectionProvider> =
      ExtensionPointName("com.github.lppedd.idea-conventional-commit.commitInspectionProvider")
  }

  override fun getInspectionProviders(): Collection<CommitInspectionProvider> =
    inspectionEpName.extensionList
}
