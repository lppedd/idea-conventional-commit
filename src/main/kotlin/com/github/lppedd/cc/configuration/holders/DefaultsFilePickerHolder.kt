package com.github.lppedd.cc.configuration.holders

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.KGridConstraints
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.configuration.component.ComponentHolder
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBCheckBox
import com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.ComponentWithEmptyText
import com.intellij.util.ui.JBUI
import org.everit.json.schema.ValidationException
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

/**
 * @author Edoardo Luppi
 */
internal class DefaultsFilePickerHolder(private val disposable: Disposable) : ComponentHolder {
  private val panel = JPanel(GridLayoutManager(2, 1, JBUI.emptyInsets(), 0, 10))
  private val isCustomFile = JBCheckBox(CCBundle["cc.config.defaults.customDefaults"])
  private val customFile = TextFieldWithBrowseButton()
  private var isValid = true

  override fun getComponent() = buildComponents()

  fun getCustomFilePath() =
    if (isCustomFile.isSelected) customFile.text
    else null

  fun setCustomFilePath(path: String?) {
    if (path != null) {
      isCustomFile.isSelected = true
      customFile.isEnabled = true
      customFile.text = FileUtil.toSystemDependentName(path)
      ComponentValidator
        .getInstance(customFile)
        .ifPresent(ComponentValidator::revalidate)
    } else {
      isCustomFile.isSelected = false
      customFile.isEnabled = false
      customFile.text = ""
    }
  }

  fun isValid() = isValid

  private fun buildComponents(): JPanel {
    installValidationOnFilePicker()

    customFile.addBrowseFolderListener(TextBrowseFolderListener(DefaultsFileChooserDescriptor()))

    setEmptyText(
      customFile.textField,
      CCBundle["cc.config.customFilePicker.disabled"]
    )

    isCustomFile.addItemListener {
      when (it.stateChange) {
        ItemEvent.SELECTED   -> {
          isValid = false
          customFile.isEnabled = true
          customFile.requestFocus()
          setEmptyText(
            customFile.textField,
            CCBundle["cc.config.customFilePicker.enabled"]
          )
        }
        ItemEvent.DESELECTED -> {
          isValid = true
          customFile.isEnabled = false
          setEmptyText(
            customFile.textField,
            CCBundle["cc.config.customFilePicker.disabled"]
          )
        }
      }
    }

    return panel.apply {
      add(isCustomFile, KGridConstraints(row = 0, fill = FILL_HORIZONTAL))
      add(customFile, KGridConstraints(row = 1, fill = FILL_HORIZONTAL))
    }
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
    if (!isCustomFile.isSelected) {
      isValid = true
      return null
    }

    val text = customFile.text.trim()

    if (text.isEmpty()) {
      isValid = false
      return ValidationInfo(CCBundle["cc.config.filePicker.error.empty"], customFile)
    }

    if (!text.endsWith("json", true)) {
      isValid = false
      return ValidationInfo(CCBundle["cc.config.filePicker.error.path"], customFile)
    }

    return try {
      CCDefaultTokensService.refreshTokens(text)
      isValid = true
      null
    } catch (e: Exception) {
      isValid = false

      val error = if (e is ValidationException) {
        val messages = e.allMessages.joinToString("<br />", ":<br />")
        CCBundle["cc.config.filePicker.error.schema"] + messages
      } else {
        CCBundle["cc.config.filePicker.error.schema"]
      }

      ValidationInfo(error, customFile)
    }
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

  private class DefaultsFileChooserDescriptor : FileChooserDescriptor(true, true, true, true, false, false) {
    init {
      withFileFilter { file: VirtualFile -> file.isValid && "json".equals(file.extension, true) }
      withTitle(CCBundle["cc.config.fileDialog.title"])
      withDescription(CCBundle["cc.config.fileDialog.description"])
    }
  }
}
