package com.github.lppedd.cc.ui

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapperPeer
import com.intellij.openapi.ui.ValidationInfo

/**
 * The sole purpose of this subclass is allowing [DialogWrapper] to use
 * our custom [CCDialogWrapperPeer], and exposing internal functionalities.
 *
 * @author Edoardo Luppi
 */
internal abstract class CCDialogWrapper(project: Project) : DialogWrapper(project), DataProvider {
  override fun createPeer(
      project: Project?,
      canBeParent: Boolean,
      ideModalityType: IdeModalityType,
  ): DialogWrapperPeer =
    CCDialogWrapperPeer(this, project, canBeParent, ideModalityType)

  override fun getData(dataId: String): CCDialogWrapper? =
    if (ValidationNavigable.DIALOG.`is`(dataId) && this is ValidationNavigable) this else null

  /**
   * Implement this interface to allow navigation of validation errors with key shortcuts.
   */
  internal interface ValidationNavigable {
    companion object {
      @JvmField
      val DIALOG: DataKey<ValidationNavigable> = DataKey.create("cc.dialog.validationNavigable")
    }

    fun validateAll(): List<ValidationInfo>
  }
}
