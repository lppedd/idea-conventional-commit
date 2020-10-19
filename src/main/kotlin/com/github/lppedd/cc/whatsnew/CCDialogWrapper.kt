package com.github.lppedd.cc.whatsnew

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapperPeer

/**
 * The sole purpose of this subclass is allowing [DialogWrapper] to use our custom [CCDialogWrapperPeer].
 *
 * @author Edoardo Luppi
 */
internal abstract class CCDialogWrapper(project: Project) : DialogWrapper(project) {
  override fun createPeer(
      project: Project?,
      canBeParent: Boolean,
      ideModalityType: IdeModalityType,
  ): DialogWrapperPeer =
    CCDialogWrapperPeer(this, project, canBeParent, ideModalityType)
}
