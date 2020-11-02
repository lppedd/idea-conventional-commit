package com.github.lppedd.cc.configuration.component

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VFileProperty.SYMLINK
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Edoardo Luppi
 */
internal open class CCFileChooserDescriptor(
    chooseFiles: Boolean = true,
    chooseFolders: Boolean = false,
    chooseJars: Boolean = false,
    chooseJarsAsFiles: Boolean = false,
    chooseJarContents: Boolean = false,
    chooseMultiple: Boolean = false,
    val okActionName: String? = null,
    val cancelActionName: String? = null,
) : FileChooserDescriptor(
  chooseFiles,
  chooseFolders,
  chooseJars,
  chooseJarsAsFiles,
  chooseJarContents,
  chooseMultiple,
) {
  override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean =
    !(file.`is`(SYMLINK) && file.canonicalPath == null ||
      !file.isDirectory && FileElement.isArchive(file) ||
      isHideIgnored && FileTypeManager.getInstance().isFileIgnored(file) ||
      !showHiddenFiles && FileElement.isFileHidden(file))
}
