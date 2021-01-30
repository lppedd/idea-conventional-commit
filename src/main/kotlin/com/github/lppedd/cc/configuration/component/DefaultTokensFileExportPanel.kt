package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.getResourceAsStream
import com.github.lppedd.cc.scaled
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class DefaultTokensFileExportPanel
  : JPanel(GridLayoutManager(1, 2, JBUI.emptyInsets(), 20.scaled, 0)),
    LinkListener<Any?> {
  private val exportAction = ActionLinkLabel(CCBundle["cc.config.defaults.exportToPath"], this)
  private val exportInfo = JBLabel()

  init {
    val gc = GridConstraints()
    gc.fill = GridConstraints.FILL_HORIZONTAL
    gc.hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
    add(exportAction, gc)

    gc.column = 1
    gc.hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
    add(exportInfo, gc)
  }

  override fun linkSelected(aSource: LinkLabel<Any?>, aLinkData: Any?) {
    val virtualFileWrapper = FileChooserFactory.getInstance()
      .createSaveFileDialog(FileSaverDescriptor(CCBundle["cc.config.exportDialog.title"], ""), null)
      .save(null as VirtualFile?, CC.Tokens.File)

    try {
      writeFile(virtualFileWrapper)
    } catch (e: Exception) {
      exportInfo.foreground = JBColor.RED
      exportInfo.text = "${CCBundle["cc.config.defaults.exportToPath.error"]} - ${e.message}"
    }
  }

  private fun writeFile(virtualFileWrapper: VirtualFileWrapper?) {
    val file = virtualFileWrapper?.file ?: return

    if (!file.exists()) {
      file.createNewFile()
    }

    val virtualFile = virtualFileWrapper.virtualFile

    if (virtualFile == null || !virtualFile.isWritable) {
      exportInfo.foreground = JBColor.RED
      exportInfo.text = CCBundle["cc.config.defaults.exportToPath.error"]
      return
    }

    getResourceAsStream("/defaults/${CC.Tokens.File}").use {
      runWriteAction {
        virtualFile.setBinaryContent(it.readBytes())
      }

      exportInfo.foreground = UIUtil.getLabelDisabledForeground()
      exportInfo.text = CCBundle["cc.config.defaults.exportToPath.completed"]
    }
  }
}
