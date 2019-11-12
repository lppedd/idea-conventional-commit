package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.ConventionalCommitBundle
import com.github.lppedd.cc.ConventionalCommitConstants
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
class ConventionalCommitConfigurable : SearchableConfigurable {
  private val config = ConventionalCommitConfig.INSTANCE
  private val gui = ConventionalCommitConfigurableGui(ConventionalCommitBundle)

  override fun getId() = "preferences.${ConventionalCommitConstants.I_APP_NAME}"
  override fun getDisplayName() = ConventionalCommitBundle["conventionalCommit.pluginName"]

  override fun apply() {
    config.state = config.state.copy(
      completionType = gui.completionType
    )
  }

  override fun reset() {
    gui.completionType = config.state.completionType
  }

  override fun isModified() =
    gui.completionType != config.state.completionType

  override fun createComponent(): JComponent? {
    gui.completionType = config.state.completionType
    return gui.rootPanel
  }
}
