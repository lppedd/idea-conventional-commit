package com.github.lppedd.cc.configuration.component

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.configuration.component.providers.CoAuthors
import com.github.lppedd.cc.configuration.holders.CoAuthorsTableHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.scale.JBUIScale
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsDialog(project: Project) : DialogWrapper(project) {
  private val defaultsService = CCDefaultTokensService.getInstance(project)
  private val coAuthorsTableHolder = CoAuthorsTableHolder(defaultsService)

  init {
    init()
    title = CCBundle["cc.config.coAuthorsDialog.title"]
    contentPane.preferredSize = Dimension(JBUIScale.scale(400), JBUIScale.scale(500))

    // It's good the dialog dimensions get saved, but it's better
    // to always display it in the center of the commit dialog
    window.addWindowListener(
      object : WindowAdapter() {
        override fun windowOpened(e: WindowEvent?) {
          centerRelativeToParent()
        }
      })
  }

  fun getSelectedAuthors(): CoAuthors =
    coAuthorsTableHolder.tableModel.selectedCoAuthors

  override fun doOKAction() {
    defaultsService.setCoAuthors(coAuthorsTableHolder.table.model.coAuthors)
    super.doOKAction()
  }

  override fun doValidate(): ValidationInfo? =
    coAuthorsTableHolder.validate().firstOrNull()

  override fun postponeValidation(): Boolean =
    false

  override fun getDimensionServiceKey(): String =
    "#com.github.lppedd.cc.CoAuthorsDialog"

  override fun getPreferredFocusedComponent(): JComponent =
    coAuthorsTableHolder.table

  override fun createCenterPanel(): JComponent =
    JPanel(BorderLayout()).also {
      it.add(coAuthorsTableHolder.getComponent(), BorderLayout.CENTER)
    }
}
