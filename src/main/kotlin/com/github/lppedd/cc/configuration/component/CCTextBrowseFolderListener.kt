package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.findRootDir
import com.github.lppedd.cc.setName
import com.intellij.openapi.fileChooser.PathChooserDialog
import com.intellij.openapi.fileChooser.impl.FileChooserFactoryImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager

/**
 * The sole purpose of this subclass is allowing the use of our custom [CCFileChooserDialogImpl].
 *
 * @author Edoardo Luppi
 */
@Suppress("UnstableApiUsage")
internal class CCTextBrowseFolderListener(
  descriptor: CCFileChooserDescriptor,
  project: Project?,
) : TextBrowseFolderListener(descriptor, project) {
  override fun run() {
    val dialog = getPathChooserDialog()
    dialog.choose(initialFile) { onFileChosen(it[0]) }
  }

  override fun getInitialFile(): VirtualFile? =
    project?.findRootDir() ?: super.getInitialFile()

  private fun getPathChooserDialog(): PathChooserDialog {
    val descriptor = myFileChooserDescriptor as CCFileChooserDescriptor
    val parentComponent = myTextComponent ?: WindowManager.getInstance().suggestParentWindow(project)

    if (!descriptor.isForcedToUseIdeaFileChooser) {
      val nativeChooser = FileChooserFactoryImpl.createNativePathChooserIfEnabled(descriptor, project, parentComponent)

      if (nativeChooser != null) {
        return nativeChooser
      }
    }

    val ideaChooser = if (parentComponent != null) {
      CCFileChooserDialogImpl(descriptor, parentComponent, project)
    } else {
      CCFileChooserDialogImpl(descriptor, project)
    }

    descriptor.okActionName?.let(ideaChooser.okAction::setName)
    descriptor.cancelActionName?.let(ideaChooser.cancelAction::setName)
    return ideaChooser
  }
}
