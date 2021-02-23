package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.annotation.Compatibility
import com.github.lppedd.cc.scaled
import com.intellij.icons.AllIcons.Actions
import com.intellij.icons.AllIcons.General
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.ui.InplaceButton
import com.intellij.ui.JBColor
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.CurrentTheme.Validator
import com.intellij.util.ui.accessibility.ScreenReader
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.Box
import javax.swing.Icon
import javax.swing.JPanel

/**
 * A banner used by [CommitBuilderDialog] to let the users know completion
 * is available on fields, and to let them remember the shortcut to use.
 * In case no shortcut is configured for completion, an error is shown instead.
 *
 * @author Edoardo Luppi
 */
internal class HowToBanner(completionShortcutText: String) : JPanel(BorderLayout(5.scaled, 0)) {
  init {
    border = JBUI.Borders.merge(
        JBUI.Borders.empty(10),
        SideBorder(JBColor.border(), SideBorder.BOTTOM, 1.scaled),
        true,
    )

    val infoIcon: Icon
    val infoText: String
    val closeText: String

    if (completionShortcutText.isNotEmpty()) {
      infoIcon = General.Information
      infoText = CCBundle["cc.commitbuilder.dialog.howTo.info", completionShortcutText]
      closeText = CCBundle["cc.commitbuilder.dialog.howTo.info.close"]

      @Compatibility(
          minVersion = "202.4357.23",
          replaceWith = "...globalScheme.getColor(HintUtil#PROMOTION_PANE_KEY)"
      )
      val color = EditorColorsManager.getInstance().globalScheme.getColor(ColorKey.find("PROMOTION_PANE"))
      background = color ?: JBColor(0xE0EAF8, 0x3B4C57)
    } else {
      infoIcon = General.Error
      infoText = CCBundle["cc.commitbuilder.dialog.howTo.error"]
      closeText = CCBundle["cc.commitbuilder.dialog.howTo.error.close"]
      background = Validator.errorBackgroundColor()
    }

    add(buildInfoIcon(infoIcon), BorderLayout.LINE_START)
    add(buildInfoText(infoText), BorderLayout.CENTER)
    add(buildCloseAction(closeText), BorderLayout.LINE_END)

    getAccessibleContext().accessibleName = CCBundle["cc.commitbuilder.dialog.a11y.howToBanner"]
  }

  private fun buildInfoIcon(infoIcon: Icon): Component =
    Box.createVerticalBox().also {
      it.add(Box.createVerticalStrut(1.scaled))
      it.add(JBLabel(infoIcon))
    }

  private fun buildInfoText(infoText: String): Component =
    JBLabel("<html>$infoText</html>").also {
      if (ScreenReader.isActive()) {
        it.isFocusable = true
      }
    }

  private fun buildCloseAction(closeText: String): Component {
    val closeButton = IconButton(closeText, Actions.Close, Actions.CloseHovered)
    val onClick: (_: ActionEvent) -> Unit = {
      PropertiesComponent.getInstance().setValue(CommitBuilderDialog.PROPERTY_HOWTO_SHOW, false, true)
      this@HowToBanner.isVisible = false
    }

    return NonOpaquePanel(BorderLayout()).also {
      it.border = JBUI.Borders.emptyLeft(10)
      it.add(InplaceButton(closeButton, onClick), BorderLayout.PAGE_START)
    }
  }
}
