package com.github.lppedd.cc.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.accessibility.ScreenReader
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import javax.swing.AbstractAction
import javax.swing.KeyStroke

/**
 * A checkbox that does not request focus when its mnemonic is used.
 *
 * Note: if screen reader support is active, the behavior is the same
 * as the platform [JBCheckBox].
 *
 * @author Edoardo Luppi
 */
internal class MnemonicAwareCheckBox(text: String) : JBCheckBox(text) {
  override fun setMnemonic(mnemonic: Int) {
    super.setMnemonic(mnemonic)

    if (mnemonic != 0 && !ScreenReader.isActive()) {
      overrideMnemonicActions()
    }
  }

  private fun overrideMnemonicActions() {
    val mnemonicMask = getSystemMnemonicKeyMask()
    val altGraphDownMask = InputEvent.ALT_GRAPH_DOWN_MASK
    val im = getInputMap(WHEN_IN_FOCUSED_WINDOW)

    // Pressed
    im.put(KeyStroke.getKeyStroke(mnemonic, mnemonicMask), "mnemonicPressed")
    im.put(KeyStroke.getKeyStroke(mnemonic, mnemonicMask or altGraphDownMask), "mnemonicPressed")

    actionMap.put("mnemonicPressed", object : AbstractAction() {
      override fun actionPerformed(event: ActionEvent) {
        model.isArmed = true
        model.isPressed = true
      }
    })

    // Released
    im.put(KeyStroke.getKeyStroke(mnemonic, mnemonicMask, true), "mnemonicReleased")
    im.put(KeyStroke.getKeyStroke(mnemonic, mnemonicMask or altGraphDownMask, true), "mnemonicReleased")

    actionMap.put("mnemonicReleased", object : AbstractAction() {
      override fun actionPerformed(event: ActionEvent) {
        model.isPressed = false
        model.isArmed = false
      }
    })
  }

  private fun getSystemMnemonicKeyMask(): Int {
    try {
      val sunToolkitClass = Class.forName("sun.awt.SunToolkit")
      val toolkit = Toolkit.getDefaultToolkit()

      if (sunToolkitClass.isInstance(toolkit)) {
        val method = sunToolkitClass.getDeclaredMethod("getFocusAcceleratorKeyMask")
        method.isAccessible = true
        return method.invoke(toolkit) as Int
      }
    } catch (_: Exception) {
      //
    }

    return ActionEvent.ALT_MASK
  }
}
