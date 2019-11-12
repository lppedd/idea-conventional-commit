package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.ConventionalCommitConstants
import com.github.lppedd.cc.configuration.ConventionalCommitConfig.CCConfig
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * @author Edoardo Luppi
 */
@State(
  name = ConventionalCommitConstants.I_APP_NAME,
  storages = [Storage(ConventionalCommitConstants.I_STORAGE_FILE)]
)
class ConventionalCommitConfig : PersistentStateComponent<CCConfig> {
  private var state = CCConfig()

  override fun getState(): CCConfig = state.copy()
  override fun loadState(state: CCConfig) {
    this.state = state
  }

  fun setState(state: CCConfig) {
    this.state = state
  }

  companion object {
    val INSTANCE: ConventionalCommitConfig by lazy {
      ApplicationManager.getApplication().getComponent(ConventionalCommitConfig::class.java)
    }
  }

  data class CCConfig(
    var completionType: CompletionType = CompletionType.AUTOPOPUP
  )

  enum class CompletionType {
    TEMPLATE,
    AUTOPOPUP
  }
}
