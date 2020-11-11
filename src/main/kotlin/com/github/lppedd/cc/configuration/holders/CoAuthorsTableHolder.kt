package com.github.lppedd.cc.configuration.holders

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCUI
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.configuration.component.providers.CoAuthorsTable
import com.github.lppedd.cc.configuration.component.providers.CoAuthorsTableModel
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.JBUI
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsTableHolder(service: CCDefaultTokensService) {
  val tableModel = CoAuthorsTableModel(service.getCoAuthors())
  val table = CoAuthorsTable(tableModel)

  private val panel = table.run {
    TableSpeedSearch(this) { value, cell -> if (cell.column == 1) value as String else null }

    val toolbarBorder = JBUI.Borders.customLine(CCUI.BorderColor, 0, 1, 0, 0)
    val panelBorder = JBUI.Borders.customLine(CCUI.BorderColor)

    ToolbarDecorator.createDecorator(this)
      .setToolbarPosition(ActionToolbarPosition.RIGHT)
      .setToolbarBorder(toolbarBorder)
      .setPanelBorder(panelBorder)
      .setRemoveAction { removeSelectedRows() }
      .setAddAction { addRow() }
      .setAddActionUpdater { !isEditing }
      .setMoveUpActionUpdater { !isEditing }
      .setMoveDownActionUpdater { !isEditing }
      .createPanel()
  }

  fun getComponent(): JPanel =
    panel

  fun validate(): Collection<ValidationInfo> =
    if (tableModel.coAuthors.indexOfFirst(String::isBlank) >= 0) {
      listOf(ValidationInfo(CCBundle["cc.config.coAuthorsDialog.error"]))
    } else {
      emptyList()
    }
}
