package com.github.lppedd.cc.configuration

import com.intellij.util.messages.Topic

/**
 * @author Edoardo Luppi
 */
internal fun interface ConfigurationChangedListener {
  companion object {
    val TOPIC: Topic<ConfigurationChangedListener> =
      Topic.create("Configuration changed", ConfigurationChangedListener::class.java)
  }

  fun onConfigurationChanged()
}
