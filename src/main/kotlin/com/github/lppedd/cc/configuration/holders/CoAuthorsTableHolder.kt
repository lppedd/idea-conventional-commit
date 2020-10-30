package com.github.lppedd.cc.configuration.holders

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCDefaultTokensService
import com.github.lppedd.cc.configuration.component.providers.CoAuthorsTable
import com.github.lppedd.cc.configuration.component.providers.CoAuthorsTableModel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.ToolbarDecorator
import javax.swing.JPanel

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsTableHolder(service: CCDefaultTokensService) {
  val tableModel = CoAuthorsTableModel(service.getCoAuthors())
  val table = CoAuthorsTable(tableModel)

  private val panel = table.run {
    TableSpeedSearch(this) { value, cell -> if (cell.column == 1) value as String else null }
    ToolbarDecorator.createDecorator(this)
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
