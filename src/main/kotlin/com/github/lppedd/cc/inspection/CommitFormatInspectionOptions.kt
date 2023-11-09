package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.wrap
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CommitFormatInspectionOptions : ConfigurableUi<Project> {
  private val charLabel = JBLabel(CCBundle["cc.inspection.nonStdMessage.replaceWs.label"])
  private val charComboModel = CharEntryModel(
      listOf(
          CharEntry("-", "Dash"),
          CharEntry(":", "Colon"),
          CharEntry("", "Remove"),
      )
  )

  private val charComboBox = ComboBox(charComboModel)
  private val myMainPanel: JPanel = JPanel().also {
    val borderColor = JBColor.namedColor("Group.separatorColor", JBColor(Gray.xCD, Gray.x51))
    it.border =
      JBUI.Borders.emptyTop(5)
        .wrap(JBUI.Borders.customLine(borderColor, 1, 0, 0, 0))
        .wrap(JBUI.Borders.emptyTop(5))

    it.layout = BoxLayout(it, BoxLayout.X_AXIS)
    it.add(charLabel)
    it.add(Box.createRigidArea(JBDimension(10, 0)))
    it.add(charComboBox)
  }

  override fun reset(project: Project) {
    val configService = project.service<CCConfigService>()
    charComboModel.setSelectedItem(configService.scopeReplaceChar)
  }

  override fun isModified(project: Project): Boolean {
    val configService = project.service<CCConfigService>()
    return charComboModel.selected.char != configService.scopeReplaceChar
  }

  override fun apply(project: Project) {
    val configService = project.service<CCConfigService>()
    configService.scopeReplaceChar = charComboModel.selected.char
  }

  override fun getComponent(): JComponent =
    myMainPanel

  private class CharEntryModel(items: List<CharEntry>) : MutableCollectionComboBoxModel<CharEntry>(items) {
    override fun getSelected(): CharEntry =
      super.getSelected()!!

    fun setSelectedItem(item: String) {
      if (mySelection.char != item) {
        this.selectedItem = this.internalList.find { it.char == item }
      }
    }
  }

  private class CharEntry(val char: String, val description: String) {
    override fun toString(): String =
      description

    override fun hashCode(): Int =
      Objects.hashCode(description)

    override fun equals(other: Any?): Boolean =
      description == (other as? CharEntry)?.description
  }
}
