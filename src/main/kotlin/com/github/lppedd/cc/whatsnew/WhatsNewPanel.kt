package com.github.lppedd.cc.whatsnew

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.WhatsNewProvider
import com.github.lppedd.cc.api.WhatsNewProvider.WhatsNewPage
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ide.util.TipUIUtil
import com.intellij.ide.util.TipUIUtil.Browser
import com.intellij.openapi.ui.DialogWrapper.DoNotAskOption
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ResourceUtil
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class WhatsNewPanel : JPanel(BorderLayout()), DoNotAskOption {
  private val browser: Browser = TipUIUtil.createBrowser()
  private lateinit var provider: WhatsNewProvider
  private var whatsNewPages: List<WhatsNewPage> = emptyList()
  private var nameIndex = 0

  init {
    browser.component.border = JBUI.Borders.empty(8, 12)
    val scrollPane = ScrollPaneFactory.createScrollPane(browser.component, true)
    scrollPane.border = JBUI.Borders.customLine(JBColor(0xd9d9d9, 0x515151), 0, 0, 1, 0)
    add(scrollPane, BorderLayout.CENTER)
  }

  override fun getPreferredSize(): Dimension =
    JBDimension(550, 270)

  override fun isToBeShown(): Boolean =
    PropertiesComponent.getInstance().getValue(WhatsNewDialog.PROPERTY_SHOW, "true").toBoolean().not()

  override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
    PropertiesComponent.getInstance().setValue(WhatsNewDialog.PROPERTY_SHOW, toBeShown.not().toString())
  }

  override fun canBeHidden(): Boolean =
    true

  override fun shouldSaveOptionsOnCancel(): Boolean =
    true

  override fun getDoNotShowMessage(): String =
    CCBundle["cc.whatsnew.dialog.show"]

  fun setProvider(provider: WhatsNewProvider) {
    this.provider = provider
    whatsNewPages = provider.getWhatsNewPages().toList()
    setInitialChangelog()
  }

  @Suppress("AddOperatorModifier")
  fun hasNewer(): Boolean =
    nameIndex > 0

  fun hasOlder(): Boolean =
    nameIndex < whatsNewPages.size - 1

  fun newerChangelog() {
    setChangelog(whatsNewPages[--nameIndex].fileName)
  }

  fun olderChangelog() {
    setChangelog(whatsNewPages[++nameIndex].fileName)
  }

  fun currentVersion(): String? =
    whatsNewPages[nameIndex].version

  fun newerVersion(): String? =
    whatsNewPages[nameIndex - 1].version

  fun olderVersion(): String? =
    whatsNewPages[nameIndex + 1].version

  private fun setInitialChangelog() {
    nameIndex = 0
    setChangelog(whatsNewPages[nameIndex].fileName)
  }

  private fun setChangelog(fileName: String) {
    require(fileName.isNotBlank()) {
      "The changelog file's name cannot be blank. Provider: ${provider::class.java.name}"
    }

    val classLoader = provider.pluginDescriptor.pluginClassLoader ?: this::class.java.classLoader
    val changelogStream = ResourceUtil.getResourceAsStream(classLoader, provider.basePath(), fileName)

    requireNotNull(changelogStream) {
      "The changelog file '$fileName' doesn't exist. Provider: ${provider::class.java.name}"
    }

    browser.text = ResourceUtil.loadText(changelogStream)
  }
}
