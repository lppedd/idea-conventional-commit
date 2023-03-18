package com.github.lppedd.cc.ui

import com.intellij.icons.AllIcons.General
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.fields.ExtendableTextComponent.Extension
import com.intellij.ui.components.fields.ExtendableTextField
import javax.swing.event.DocumentEvent

/**
 * @author Edoardo Luppi
 */
internal class VerifiableExtendableTextField(
    columns: Int,
    private val verifier: (VerifiableExtendableTextField) -> Boolean,
    private val errorTooltipText: String,
) : ExtendableTextField(columns) {
  private val errorIconExtension = Extension {
    General.BalloonError
  }

  var isContentValid = true

  init {
    document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(event: DocumentEvent) {
        if (verifier(this@VerifiableExtendableTextField)) {
          showError()
        } else {
          hideError()
        }
      }
    })
  }

  private fun showError() {
    if (!isContentValid) {
      isContentValid = true
      removeExtension(errorIconExtension)
      putClientProperty("JComponent.outline", null)
      toolTipText = null
    }
  }

  private fun hideError() {
    if (isContentValid) {
      isContentValid = false
      addExtension(errorIconExtension)
      putClientProperty("JComponent.outline", "error")
      toolTipText = errorTooltipText
    }
  }
}
