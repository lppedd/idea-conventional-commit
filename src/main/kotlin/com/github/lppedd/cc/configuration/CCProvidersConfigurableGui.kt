package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.CommitSubjectProvider
import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.configuration.component.providers.CommitProviderTable
import com.github.lppedd.cc.ui.JBGridLayout
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.util.containers.Convertor
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CCProvidersConfigurableGui {
  val rootPanel = JPanel(BorderLayout(0, 15))

  private val types = CommitProviderTable<CommitTypeProvider>(CCBundle["cc.config.providers.type.title"])
  private val scopes = CommitProviderTable<CommitScopeProvider>(CCBundle["cc.config.providers.scope.title"])
  private val subjects =
    CommitProviderTable<CommitSubjectProvider>(CCBundle["cc.config.providers.subject.title"])

  init {
    finishUpComponents()
  }

  val typeProviders: List<CommitTypeProvider>
    get() = types.providers

  val scopeProviders: List<CommitScopeProvider>
    get() = scopes.providers

  val subjectProviders: List<CommitSubjectProvider>
    get() = subjects.providers

  fun setProviders(
      types: List<CommitTypeProvider>,
      scopes: List<CommitScopeProvider>,
      subjects: List<CommitSubjectProvider>,
  ) {
    this.types.providers = types
    this.scopes.providers = scopes
    this.subjects.providers = subjects
  }

  val isModified: Boolean
    get() = types.isModified() || scopes.isModified() || subjects.isModified()

  fun reset() {
    types.reset()
    scopes.reset()
    subjects.reset()
  }

  private fun finishUpComponents() {
    TableSpeedSearch(types, Convertor { (it as CommitTypeProvider).getPresentation().name })
    TableSpeedSearch(scopes, Convertor { (it as CommitScopeProvider).getPresentation().name })
    TableSpeedSearch(subjects, Convertor { (it as CommitSubjectProvider).getPresentation().name })

    val providersPanel = JPanel(JBGridLayout(3, 1, 0, 10)).also {
      it.add(
        ToolbarDecorator
          .createDecorator(types)
          .setRemoveAction(null)
          .setAddAction(null)
          .createPanel()
      )
      it.add(
        ToolbarDecorator
          .createDecorator(scopes)
          .setRemoveAction(null)
          .setAddAction(null)
          .createPanel()
      )
      it.add(
        ToolbarDecorator
          .createDecorator(subjects)
          .setRemoveAction(null)
          .setAddAction(null)
          .createPanel()
      )
    }

    rootPanel.add(JBLabel(CCBundle["cc.config.providersPriority"]), BorderLayout.NORTH)
    rootPanel.add(providersPanel, BorderLayout.CENTER)
  }
}
