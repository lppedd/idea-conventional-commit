package com.github.lppedd.cc.configuration.holders

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.DEFAULT_FILE
import com.github.lppedd.cc.configuration.component.ActionLinkLabel
import com.github.lppedd.cc.getResourceAsStream
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
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class DefaultsFileExportHolder : ComponentHolder, LinkListener<Any?> {
  private val exportAction = ActionLinkLabel(CCBundle["cc.config.defaults.exportToPath"], this)
  private val exportInfo = JBLabel()
  private val panel = JPanel(GridLayoutManager(1, 2, JBUI.insets(0, 1, 6, 0), 20, 0)).apply {
    val gc = GridConstraints()
    gc.fill = GridConstraints.FILL_HORIZONTAL
    gc.hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
    add(exportAction, gc)

    gc.column = 1
    gc.hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
    add(exportInfo, gc)
  }

  override fun getComponent(): JComponent = panel
  override fun linkSelected(aSource: LinkLabel<Any?>, aLinkData: Any?) {
    val virtualFileWrapper = FileChooserFactory.getInstance()
      .createSaveFileDialog(FileSaverDescriptor(CCBundle["cc.config.exportDialog.title"], ""), null)
      .save(null as VirtualFile?, DEFAULT_FILE)

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

    getResourceAsStream("/defaults/${DEFAULT_FILE}").use {
      runWriteAction {
        virtualFile.setBinaryContent(it.readBytes())
      }

      exportInfo.foreground = UIUtil.getLabelDisabledForeground()
      exportInfo.text = CCBundle["cc.config.defaults.exportToPath.completed"]
    }
  }
}
