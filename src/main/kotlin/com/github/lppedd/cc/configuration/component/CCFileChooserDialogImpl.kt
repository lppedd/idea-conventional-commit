package com.github.lppedd.cc.configuration.component

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.project.Project
import java.awt.Component
import javax.swing.Action

/**
 * @author Edoardo Luppi
 */
internal class CCFileChooserDialogImpl : FileChooserDialogImpl {
  constructor(descriptor: FileChooserDescriptor, parent: Component, project: Project? = null)
      : super(descriptor, parent, project)

  constructor(descriptor: FileChooserDescriptor, project: Project? = null)
      : super(descriptor, project)

  public override fun getOKAction(): Action =
    super.getOKAction()

  public override fun getCancelAction(): Action =
    super.getCancelAction()
}
