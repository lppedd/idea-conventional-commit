package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.setName
import com.intellij.openapi.fileChooser.PathChooserDialog
import com.intellij.openapi.fileChooser.impl.FileChooserFactoryImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.wm.WindowManager

/**
 * The sole purpose of this subclass is allowing the use of our custom [CCFileChooserDialogImpl].
 *
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CCTextBrowseFolderListener(
    private val fileChooserDescriptor: CCFileChooserDescriptor,
    project: Project? = null,
) : TextBrowseFolderListener(fileChooserDescriptor, project) {
  override fun run() {
    val dialog = getPathChooserDialog()
    dialog.choose(initialFile) { chosenFiles ->
      onFileChosen(chosenFiles[0])
    }
  }

  private fun getPathChooserDialog(): PathChooserDialog {
    val parentComponent = if (myTextComponent != null) {
      myTextComponent
    } else {
      WindowManager.getInstance().suggestParentWindow(project)
    }

    val nativePathChooser = FileChooserFactoryImpl.createNativePathChooserIfEnabled(
        myFileChooserDescriptor,
        project,
        parentComponent,
    )

    if (nativePathChooser != null) {
      return nativePathChooser
    }

    val chooser = if (parentComponent != null) {
      CCFileChooserDialogImpl(myFileChooserDescriptor, parentComponent, project)
    } else {
      CCFileChooserDialogImpl(myFileChooserDescriptor, project)
    }

    fileChooserDescriptor.okActionName?.let(chooser.okAction::setName)
    fileChooserDescriptor.cancelActionName?.let(chooser.cancelAction::setName)

    return chooser
  }
}
