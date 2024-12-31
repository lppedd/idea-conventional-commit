package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCUI
import com.github.lppedd.cc.api.*
import com.github.lppedd.cc.configuration.component.providers.CommitProviderTable
import com.github.lppedd.cc.scaled
import com.github.lppedd.cc.ui.ScaledGridLayout
import com.github.lppedd.cc.ui.TitledPanel
import com.intellij.openapi.actionSystem.ActionToolbarPosition.RIGHT
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CCProvidersConfigurableGui {
  val rootPanel = JPanel(BorderLayout(0, 15))

  // @formatter:off
  private val types = CommitProviderTable<CommitTypeProvider>()
  private val scopes = CommitProviderTable<CommitScopeProvider>()
  private val subjects = CommitProviderTable<CommitSubjectProvider>()
  private val bodies = CommitProviderTable<CommitBodyProvider>()
  private val footerTypes = CommitProviderTable<CommitFooterTypeProvider>()
  private val footerValues = CommitProviderTable<CommitFooterValueProvider>()
  // @formatter:on

  init {
    finishUpComponents()
  }

  val typeProviders: List<CommitTypeProvider>
    get() = types.providers

  val scopeProviders: List<CommitScopeProvider>
    get() = scopes.providers

  val subjectProviders: List<CommitSubjectProvider>
    get() = subjects.providers

  val bodyProviders: List<CommitBodyProvider>
    get() = bodies.providers

  val footerTypeProviders: List<CommitFooterTypeProvider>
    get() = footerTypes.providers

  val footerValueProviders: List<CommitFooterValueProvider>
    get() = footerValues.providers

  fun setProviders(
      types: List<CommitTypeProvider>,
      scopes: List<CommitScopeProvider>,
      subjects: List<CommitSubjectProvider>,
      bodies: List<CommitBodyProvider>,
      footerTypes: List<CommitFooterTypeProvider>,
      footerValues: List<CommitFooterValueProvider>,
  ) {
    this.types.providers = types
    this.scopes.providers = scopes
    this.subjects.providers = subjects
    this.bodies.providers = bodies
    this.footerTypes.providers = footerTypes
    this.footerValues.providers = footerValues
  }

  val isModified: Boolean
    get() = types.isModified() ||
            scopes.isModified() ||
            subjects.isModified() ||
            bodies.isModified() ||
            footerTypes.isModified() ||
            footerValues.isModified()

  fun reset() {
    types.reset()
    scopes.reset()
    subjects.reset()
    bodies.reset()
    footerTypes.reset()
    footerValues.reset()
  }

  private fun finishUpComponents() {
    val providersPanel = JPanel(ScaledGridLayout(6, 1, 0, 15)).also {
      it.add(buildTablePanel(types, CCBundle["cc.config.providers.type.title"]))
      it.add(buildTablePanel(scopes, CCBundle["cc.config.providers.scope.title"]))
      it.add(buildTablePanel(subjects, CCBundle["cc.config.providers.subject.title"]))
      it.add(buildTablePanel(bodies, CCBundle["cc.config.providers.body.title"]))
      it.add(buildTablePanel(footerTypes, CCBundle["cc.config.providers.footerType.title"]))
      it.add(buildTablePanel(footerValues, CCBundle["cc.config.providers.footerValue.title"]))
    }

    rootPanel.add(JBLabel(CCBundle["cc.config.providersPriority"]), BorderLayout.NORTH)
    rootPanel.add(providersPanel, BorderLayout.CENTER)
  }

  private fun buildTablePanel(table: JBTable, title: String): JPanel {
    TableSpeedSearch.installOn(table) { value -> (value as CommitTokenProvider).getPresentation().getName() }

    val toolbarBorder = JBUI.Borders.customLine(CCUI.BorderColor, 0, 1, 0, 0)
    val panelBorder = JBUI.Borders.customLine(CCUI.BorderColor)
    val tablePanel = ToolbarDecorator.createDecorator(table)
      .setToolbarPosition(RIGHT)
      .setToolbarBorder(toolbarBorder)
      .setPanelBorder(panelBorder)
      .setPreferredSize(JBDimension(table.preferredSize.width, 138.scaled, true))
      .setRemoveAction(null)
      .setAddAction(null)
      .createPanel()

    return TitledPanel(title, tablePanel)
  }
}
