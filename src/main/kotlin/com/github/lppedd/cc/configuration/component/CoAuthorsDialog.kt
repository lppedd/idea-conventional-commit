package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCTokensService
import com.github.lppedd.cc.configuration.CoAuthorsResult
import com.github.lppedd.cc.configuration.holders.CoAuthorsTableHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.util.ui.JBDimension
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsDialog(private val project: Project, coAuthors: Set<String>) : DialogWrapper(project) {
  private val coAuthorsTableHolder = CoAuthorsTableHolder(coAuthors)

  init {
    init()
    title = CCBundle["cc.config.coAuthorsDialog.title"]
    contentPane.preferredSize = JBDimension(400, 500)

    // It's good the dialog dimensions get saved, but it's better
    // to always display it in the center of the commit dialog
    window.addWindowListener(object : WindowAdapter() {
      override fun windowOpened(e: WindowEvent?) {
        centerRelativeToParent()
      }
    })
  }

  fun getSelectedAuthors(): List<String> =
    coAuthorsTableHolder.tableModel.selectedCoAuthors

  override fun doOKAction() {
    val tokensService = CCTokensService.getInstance(project)
    val coAuthors = coAuthorsTableHolder.tableModel.coAuthors.toSet()

    when (val result = tokensService.setCoAuthors(coAuthors)) {
      is CoAuthorsResult.Success -> super.doOKAction()
      is CoAuthorsResult.Failure -> {
        val message = CCBundle["cc.config.coAuthorsDialog.saveError", result.message]
        setErrorText(message)
      }
    }
  }

  override fun doValidate(): ValidationInfo? =
    coAuthorsTableHolder.validate().firstOrNull()

  override fun getDimensionServiceKey(): String =
    "#com.github.lppedd.cc.CoAuthorsDialog"

  override fun getPreferredFocusedComponent(): JComponent =
    coAuthorsTableHolder.table

  override fun createCenterPanel(): JComponent =
    JPanel(BorderLayout()).also {
      it.add(coAuthorsTableHolder.getComponent(), BorderLayout.CENTER)
    }
}
