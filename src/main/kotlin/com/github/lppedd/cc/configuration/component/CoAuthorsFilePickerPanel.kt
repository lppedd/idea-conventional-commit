package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.scaled
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBCheckBox
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.ComponentWithEmptyText
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UI
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsFilePickerPanel(
    private val project: Project,
    private val disposable: Disposable,
) : JPanel(GridLayoutManager(2, 1, JBUI.emptyInsets(), 0, 5.scaled)) {
  private val isCustomFile = JBCheckBox(CCBundle["cc.config.customFile"]).also {
    it.addItemListener { event ->
      when (event.stateChange) {
        ItemEvent.SELECTED -> customFileChecked()
        ItemEvent.DESELECTED -> customFileUnchecked()
      }
    }
  }

  private val customFile = TextFieldWithBrowseButton().also {
    it.isEnabled = false
    it.addBrowseFolderListener(CCTextBrowseFolderListener(MyFileChooserDescriptor, project))
  }

  var isComponentValid: Boolean = true
    private set

  init {
    installValidationOnFilePicker()
    setEmptyText(customFile.textField, CCBundle["cc.config.coAuthors.customFile.disabled"])

    val gc = GridConstraints()
    gc.fill = FILL_HORIZONTAL
    add(buildIsCustomFilePanel(), gc)

    gc.row = 1
    add(buildCustomFilePanel(), gc)
  }

  fun getCustomFilePath(): String? =
    customFile.text.takeIf { isCustomFile.isSelected }

  @Suppress("DuplicatedCode")
  fun setCustomFilePath(path: String?) {
    if (path != null) {
      isCustomFile.isSelected = true
      customFile.isEnabled = true
      customFile.text = FileUtil.toSystemDependentName(path)
      ComponentValidator.getInstance(customFile).ifPresent(ComponentValidator::revalidate)
    } else {
      isCustomFile.isSelected = false
      customFile.isEnabled = false
      customFile.text = ""
    }
  }

  fun revalidateComponent() {
    ComponentValidator.getInstance(customFile).get().revalidate()
  }

  private fun customFileChecked() {
    isComponentValid = false
    customFile.isEnabled = true
    customFile.requestFocus()
    setEmptyText(customFile.textField, CCBundle["cc.config.customFilePicker.enabled"])
  }

  private fun customFileUnchecked() {
    isComponentValid = true
    customFile.isEnabled = false
    setEmptyText(customFile.textField, CCBundle["cc.config.coAuthors.customFile.disabled"])
  }

  private fun installValidationOnFilePicker() {
    ComponentValidator(disposable)
      .withValidator(::customFileValidator)
      .withFocusValidator(::customFileValidator)
      .withOutlineProvider { customFile.textField }
      .installOn(customFile)

    customFile.textField.document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(e: DocumentEvent) {
        ComponentValidator
          .getInstance(customFile)
          .ifPresent(ComponentValidator::revalidate)
      }
    })
  }

  private fun customFileValidator(): ValidationInfo? {
    isComponentValid = true

    if (!isCustomFile.isSelected) {
      return null
    }

    customFile.text.trim().let {
      if (it.isEmpty()) {
        isComponentValid = false
        return ValidationInfo(CCBundle["cc.config.filePicker.error.empty"], customFile)
      }

      if (!it.endsWith("coauthors", true)) {
        isComponentValid = false
        return ValidationInfo(CCBundle["cc.config.filePicker.error.path"], customFile)
      }
    }

    return null
  }

  private fun setEmptyText(component: JComponent, text: String?) {
    if (component !is ComponentWithEmptyText) {
      return
    }

    if (text != null) {
      component.emptyText.setText(text)
    } else {
      component.emptyText.clear()
    }
  }

  private fun buildIsCustomFilePanel(): JPanel =
    UI.PanelFactory.panel(isCustomFile)
      .withTooltip(CCBundle["cc.config.coAuthors.customFile.tooltip"])
      .createPanel()

  private fun buildCustomFilePanel(): JPanel =
    UI.PanelFactory.panel(customFile)
      .withComment(CCBundle["cc.config.coAuthors.customFile.comment"])
      .createPanel()

  private object MyFileChooserDescriptor : CCFileChooserDescriptor() {
    override val okActionName = "Select file"
    override val validFileIcon = CCIcons.FileTypes.CoAuthors
    override val validFileTest: (VirtualFile) -> Boolean = {
      it.isValid && !it.isDirectory && it.isWritable && "coauthors".equals(it.extension, true)
    }

    init {
      withFileFilter(validFileTest)
      withTitle(CCBundle["cc.config.coAuthors.customFile.dialog.title"])
      withDescription(CCBundle["cc.config.coAuthors.customFile.dialog.description"])
    }
  }
}
