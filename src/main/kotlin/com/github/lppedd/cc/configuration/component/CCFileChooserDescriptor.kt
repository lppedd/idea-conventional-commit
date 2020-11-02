package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.isSymlink
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

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
) : FileChooserDescriptor(
  chooseFiles,
  chooseFolders,
  chooseJars,
  chooseJarsAsFiles,
  chooseJarContents,
  chooseMultiple,
) {
  open val okActionName: String? = null
  open val cancelActionName: String? = null
  open val validFileIcon: Icon? = null
  open val validFileTest: ((VirtualFile) -> Boolean)? = null

  override fun getIcon(file: VirtualFile): Icon {
    val test = validFileTest
    val icon = validFileIcon

    if (test == null || icon == null) {
      return super.getIcon(file)
    }

    return when {
      test(file) -> icon
      FileElement.isFileHidden(file) ->
        if (file.isDirectory) super.getIcon(file)
        else IconLoader.getTransparentIcon(CCIcons.FileTypes.Generic)
      file.isDirectory.not() -> IconLoader.getTransparentIcon(CCIcons.FileTypes.Generic, 0.82F)
      else -> IconLoader.getTransparentIcon(super.getIcon(file), 0.82F)
    }
  }

  override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean =
    !(file.isSymlink && file.canonicalPath == null ||
      !file.isDirectory && FileElement.isArchive(file) ||
      isHideIgnored && FileTypeManager.getInstance().isFileIgnored(file) ||
      !showHiddenFiles && FileElement.isFileHidden(file))
}
