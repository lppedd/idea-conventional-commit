package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.*
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.io.Reader
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class DefaultTokensFileExportPanel(private val project: Project) :
    JPanel(GridLayoutManager(1, 2, JBUI.emptyInsets(), 20.scaled, 0)),
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
    val descriptor = FileSaverDescriptor(CCBundle["cc.config.exportDialog.title"], "").also {
      it.isForcedToUseIdeaFileChooser = true
    }

    val fileWrapper = FileChooserFactory.getInstance()
      .createSaveFileDialog(descriptor, project)
      .save(project.findRootDir(), CC.File.Defaults)

    if (fileWrapper == null) {
      return
    }

    try {
      writeFile(fileWrapper)
    } catch (e: Exception) {
      exportInfo.foreground = JBColor.RED
      exportInfo.text = "${CCBundle["cc.config.defaults.exportToPath.error"]} - ${e.message}"
    }
  }

  private fun writeFile(fileWrapper: VirtualFileWrapper) {
    val virtualFile = fileWrapper.getVirtualFile(/* createIfNotExist = */ true)

    if (virtualFile == null || !virtualFile.isWritable) {
      exportInfo.foreground = JBColor.RED
      exportInfo.text = CCBundle["cc.config.defaults.exportToPath.error"]
      return
    }

    // When exporting to a file, we also need to add the JSON schema reference.
    // Better normalize line endings to \n.
    val jsonStr = StringUtil.convertLineSeparators(
      getResourceAsStream("/defaults/${CC.File.Defaults}").bufferedReader().use(Reader::readText)
    )

    val pluginVersion = getPluginVersion()
    val schemaPath = "src/main/resources/defaults/conventionalcommit.schema.json"
    val schemaUrl = "https://github.com/lppedd/idea-conventional-commit/raw/$pluginVersion/$schemaPath"
    val sb = StringBuilder(jsonStr)
    sb.insert(4, $$"\"$schema\": \"$$schemaUrl\",\n  ")

    WriteAction.runAndWait<Throwable> {
      virtualFile.setBinaryContent("$sb".toByteArray())
    }

    exportInfo.foreground = UIUtil.getLabelDisabledForeground()
    exportInfo.text = CCBundle["cc.config.defaults.exportToPath.completed"]
  }

  private fun getPluginVersion(): String {
    val plugin = PluginManagerCore.getPlugin(PluginId.getId(CC.PluginId)) ?: error("plugin not found")
    return plugin.version
  }
}
