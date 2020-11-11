package com.github.lppedd.cc.configuration.component.providers

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.selectedIndices
import com.github.lppedd.cc.ui.CCTable
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.JTable
import javax.swing.KeyStroke
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.table.TableCellRenderer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author Edoardo Luppi
 */
internal class CoAuthorsTable(tableModel: CoAuthorsTableModel) : CCTable(tableModel) {
  private companion object {
    private const val COLUMN_CHECKBOX = 0
    private const val COLUMN_TEXT = 1
  }

  init {
    if (model.rowCount > 0) {
      setRowSelectionInterval(0, 0)
    }

    emptyText.text = CCBundle["cc.config.coAuthorsDialog.empty"]

    setTableHeader(null)
    setShowGrid(false)
    setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN)

    getColumnModel().getColumn(COLUMN_CHECKBOX).also {
      it.cellRenderer = CheckboxCellRenderer()
    }

    getColumnModel().getColumn(COLUMN_TEXT).also {
      it.cellRenderer = CoAuthorCellRenderer()
      it.minWidth = 100
    }

    resizeColumnsWidthToFitContent()
    preventResizingOfCheckboxColumn()
  }

  fun removeSelectedRows() {
    stopEditing()

    if (selectedRows.isNotEmpty()) {
      val firstIndex = selectedRows.first()
      selectedRows.reversed().forEach(model::removeRow)

      val indexToSelect = firstIndex.coerceAtMost(model.rowCount - 1)
      if (indexToSelect >= 0) setRowSelectionInterval(indexToSelect, indexToSelect)
    }
  }

  fun addRow() {
    stopEditing()
    model.addRow()
    setRowSelectionInterval(0, 0)
    editCellAt(0, COLUMN_TEXT)
  }

  override fun getModel(): CoAuthorsTableModel =
    super.getModel() as CoAuthorsTableModel

  override fun editCellAt(rowIndex: Int, columnIndex: Int, event: EventObject?): Boolean =
    super.editCellAt(rowIndex, if (event is InputEvent) columnIndex else COLUMN_TEXT, event)

  override fun processKeyBinding(ks: KeyStroke?, e: KeyEvent?, condition: Int, pressed: Boolean): Boolean {
    val result = super.processKeyBinding(ks, e, condition, pressed)
    return if (
        !isEditing &&
        ks?.isOnKeyRelease == true &&
        ks.keyCode == KeyEvent.VK_SPACE &&
        ks.modifiers == 0
    ) {
      toggleSelectedRows()
      true
    } else {
      result
    }
  }

  private fun toggleSelectedRows() {
    val selectedIndices = getSelectionModel().selectedIndices()

    if (selectedIndices.isNotEmpty()) {
      val isFirstSelected = !(model.getValueAt(selectedIndices.first(), COLUMN_CHECKBOX) as Boolean)
      selectedIndices.forEach { model.setValueAt(isFirstSelected, it, COLUMN_CHECKBOX) }
    }
  }

  override fun editingCanceled(e: ChangeEvent?) {
    checkEditedRowAfter { super.editingCanceled(e) }
  }

  override fun editingStopped(e: ChangeEvent?) {
    checkEditedRowAfter { super.editingStopped(e) }
  }

  private fun checkEditedRowAfter(doBefore: () -> Unit) {
    val tempEditingRow = editingRow
    doBefore()
    model.removeRowIfEmpty(tempEditingRow)

    val indexToSelect = min(tempEditingRow, rowCount - 1)
    if (indexToSelect >= 0) setRowSelectionInterval(indexToSelect, indexToSelect)
  }

  private fun stopEditing() {
    cellEditor?.stopCellEditing()
  }

  private fun resizeColumnsWidthToFitContent() {
    val minimumWidth = 15
    val maxWidth = 300
    val columnModel = columnModel

    for (column in 0 until columnCount) {
      var width = minimumWidth

      for (row in 0 until rowCount) {
        val renderer = getCellRenderer(row, column)
        val comp = prepareRenderer(renderer, row, column)
        width = max(comp.preferredSize.width + 1, width)
      }

      width = min(maxWidth, width)
      columnModel.getColumn(column).preferredWidth = width
    }
  }

  private fun preventResizingOfCheckboxColumn() {
    val firstColumn = columnModel.getColumn(COLUMN_CHECKBOX)
    val width = (firstColumn.preferredWidth * 1.5).roundToInt()
    firstColumn.maxWidth = width
    firstColumn.minWidth = width
    firstColumn.width = width
  }

  private class CoAuthorCellRenderer : ColoredTableCellRenderer() {
    override fun customizeCellRenderer(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        rowIndex: Int,
        columnIndex: Int,
    ) {
      ipad = JBUI.insetsLeft(5)
      border = JBUI.Borders.empty(1, 3, 2, 3)

      append(value as String)
      SpeedSearchUtil.applySpeedSearchHighlighting(table, this, true, isSelected)
    }
  }

  private class CheckboxCellRenderer : JBCheckBox(), TableCellRenderer {
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isRowSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
      requireNotNull(table) { "The table object should be valid here" }

      isSelected = value as? Boolean == true
      border = JBUI.Borders.emptyRight(1)
      horizontalAlignment = SwingConstants.CENTER

      if (isRowSelected) {
        foreground = table.selectionForeground
        background = table.selectionBackground
      } else {
        foreground = table.foreground
        background = table.background
      }

      return this
    }
  }
}
