// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.scaled
import com.github.lppedd.cc.ui.VerifiableExtendableTextField
import com.github.lppedd.cc.wrap
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.Project
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

/**
 * @author Edoardo Luppi
 * @see CommitNamingConventionInspection
 */
internal class CommitNamingConventionInspectionOptions : ConfigurableUi<Project> {
  private val check = { f: VerifiableExtendableTextField ->
    try {
      Pattern.compile(f.text)
      true
    } catch (_: PatternSyntaxException) {
      false
    }
  }

  private val typePatternTextField = VerifiableExtendableTextField(
    20,
    check,
    CCBundle["cc.inspection.namingConvention.pattern.error"],
  )

  private val scopePatternTextField = VerifiableExtendableTextField(
    20,
    check,
    CCBundle["cc.inspection.namingConvention.pattern.error"],
  )

  override fun reset(project: Project) {
    val config = project.service<CCConfigService>()
    typePatternTextField.text = config.typeNamingPattern
    scopePatternTextField.text = config.scopeNamingPattern
  }

  override fun isModified(project: Project): Boolean {
    val config = project.service<CCConfigService>()
    return typePatternTextField.isContentValid &&
           scopePatternTextField.isContentValid && (
               typePatternTextField.text != config.typeNamingPattern ||
               scopePatternTextField.text != config.scopeNamingPattern)
  }

  override fun apply(project: Project) {
    val config = project.service<CCConfigService>()
    config.typeNamingPattern = typePatternTextField.text
    config.scopeNamingPattern = scopePatternTextField.text
  }

  override fun getComponent(): JComponent =
    JPanel(BorderLayout()).also {
      val borderColor = JBColor.namedColor("Group.separatorColor", JBColor(Gray.xCD, Gray.x51))
      it.border = JBUI.Borders.emptyTop(5).wrap(JBUI.Borders.customLine(borderColor, 1, 0, 0, 0))

      val panel = JPanel(BorderLayout())
      panel.border = JBUI.Borders.empty(4, 0)

      val infoBox = JBBox.createHorizontalBox()
      infoBox.add(JBLabel("<html>" + CCBundle["cc.inspection.namingConvention.comment"] + "</html>"))
      infoBox.add(JBBox.createHorizontalGlue())

      val mainBox = JBBox.createVerticalBox()
      mainBox.add(infoBox)
      mainBox.add(JBBox.createVerticalStrut(UIUtil.LARGE_VGAP.scaled))
      mainBox.add(buildPatternsPanel())

      panel.add(mainBox, BorderLayout.CENTER)

      val scrollPane = JBScrollPane(panel)
      scrollPane.border = JBUI.Borders.empty()
      scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER

      it.add(scrollPane, BorderLayout.CENTER)
    }

  private fun buildPatternsPanel(): JPanel =
    JPanel(GridBagLayout()).also {
      val gc = GridBagConstraints()
      gc.fill = GridBagConstraints.HORIZONTAL
      gc.anchor = GridBagConstraints.LINE_START
      gc.insets = JBUI.insetsRight(UIUtil.DEFAULT_HGAP)
      it.add(JBLabel(CCBundle["cc.inspection.namingConvention.pattern.type.label"]), gc)

      gc.gridx = 1
      gc.weightx = 1.0
      gc.insets = JBUI.emptyInsets()
      it.add(typePatternTextField, gc)

      gc.weightx = 0.0
      gc.gridx = 0
      gc.gridy = 1
      gc.insets = JBUI.insetsRight(UIUtil.DEFAULT_HGAP)
      it.add(JBLabel(CCBundle["cc.inspection.namingConvention.pattern.scope.label"]), gc)

      gc.weightx = 1.0
      gc.gridx = 1
      gc.insets = JBUI.emptyInsets()
      it.add(scopePatternTextField, gc)
    }
}
