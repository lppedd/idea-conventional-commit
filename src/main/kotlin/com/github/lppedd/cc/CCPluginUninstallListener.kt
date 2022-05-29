package com.github.lppedd.cc

import com.github.lppedd.cc.vcs.commitbuilder.CommitBuilderDialog
import com.github.lppedd.cc.whatsnew.InternalWhatsNewProvider
import com.github.lppedd.cc.whatsnew.WhatsNewDialog
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.ide.util.PropertiesComponent

/**
 * Listens to plugin uninstallation for cleaning-up things.
 *
 * @author Edoardo Luppi
 */
internal class CCPluginUninstallListener : PluginStateListener {
  override fun uninstall(descriptor: IdeaPluginDescriptor) {
    if (descriptor.pluginId.idString != CC.PluginId) {
      return
    }

    cleanupOptions()
  }

  override fun install(descriptor: IdeaPluginDescriptor) {
    // Can't do anything here
  }

  private fun cleanupOptions() = try {
    PropertiesComponent.getInstance().let {
      it.unsetValue(CommitBuilderDialog.PROPERTY_HOWTO_SHOW)
      it.unsetValue(InternalWhatsNewProvider.PROPERTY_VERSION)
      it.unsetValue(WhatsNewDialog.PROPERTY_SHOW)
    }
  } catch (_: Exception) {
    //
  }
}
