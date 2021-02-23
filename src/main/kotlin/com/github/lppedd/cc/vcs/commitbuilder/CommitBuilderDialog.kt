package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.CC.Provider
import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.api.FOOTER_TYPE_EP
import com.github.lppedd.cc.api.FOOTER_VALUE_EP
import com.github.lppedd.cc.completion.providers.*
import com.github.lppedd.cc.completion.resultset.TextFieldResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitFooterTypeLookupElement
import com.github.lppedd.cc.lookupElement.CommitFooterValueLookupElement
import com.github.lppedd.cc.parser.CommitContext.*
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitFooterTypePsiElement
import com.github.lppedd.cc.psiElement.CommitFooterValuePsiElement
import com.github.lppedd.cc.scaled
import com.github.lppedd.cc.setName
import com.github.lppedd.cc.ui.CCDialogWrapper
import com.github.lppedd.cc.ui.CCDialogWrapper.ValidationNavigable
import com.github.lppedd.cc.ui.MnemonicAwareCheckBox
import com.github.lppedd.cc.vcs.commitbuilder.CommitBuilderService.CommitFooter
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.DialogWrapper.DoNotAskOption
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.TextRange
import com.intellij.ui.AncestorListenerAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.border.CustomLineBorder
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.*
import com.intellij.util.ui.accessibility.ScreenReader
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.accessibility.AccessibleRelation
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder
import javax.swing.event.AncestorEvent
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * @author Edoardo Luppi
 */
internal class CommitBuilderDialog(private val project: Project)
  : CCDialogWrapper(project),
    DoNotAskOption,
    ValidationNavigable {
  companion object {
    const val PROPERTY_HOWTO_SHOW = "com.github.lppedd.cc.commitbuilder.howto.show"
  }

  private val configService = project.service<CCConfigService>()
  private val commitMessageService = project.service<CommitBuilderService>()

  private val typeTextField = CommitTokenTextField(project, CommitTypeCompletionProvider())
  private val scopeTextField = CommitTokenTextField(project, CommitScopeCompletionProvider())
  private val subjectTextField = CommitTokenTextField(project, CommitSubjectCompletionProvider())
  private val bodyTextField = CommitTokenTextField(project, CommitBodyCompletionProvider(), lines = 3)
  private val breakingChangeCheckBox = MnemonicAwareCheckBox(CCBundle["cc.commitbuilder.dialog.breakingChange"])
  private val breakingChangeTextField = CommitTokenTextField(
      project,
      CommitFooterValueCompletionProvider { "BREAKING CHANGE" },
      lines = 3,
  )
  private val footerTypeTextField = CommitTokenTextField(project, CommitFooterTypeCompletionProvider())
  private val footerValueTextField = CommitTokenTextField(
      project,
      CommitFooterValueCompletionProvider { footerTypeTextField.text },
  )

  private val breakingChangeRegex = Regex("BREAKING[- ]CHANGE", IGNORE_CASE)

  init {
    title = CCBundle["cc.commitbuilder.title"]
    isModal = true
    setDoNotAskOption(this)

    // Since we cannot workaround IDEA-262736, just keep the text field enabled
    // to avoid trapping focus
    breakingChangeTextField.isEnabled = ScreenReader.isActive()
    breakingChangeTextField.setPlaceholder(CCBundle["cc.commitbuilder.dialog.breakingChange.description"])

    // Because of the above consideration, it also doesn't make sense
    // to toggle text field activation with the checkbox
    if (!ScreenReader.isActive()) {
      breakingChangeCheckBox.addItemListener {
        breakingChangeTextField.isEnabled = breakingChangeCheckBox.isSelected
      }
    }

    footerTypeTextField.setPlaceholder(CCBundle["cc.commitbuilder.dialog.footerType"])
    footerValueTextField.setPlaceholder(CCBundle["cc.commitbuilder.dialog.footerValue"])

    init()
    initAccessibility()
    restoreFieldsValues()
    installValidators()

    // Trick to avoid spawning with a scrollbar.
    pack()

    // Needed for 192, as the default button is set to null somehow, probably on pack
    rootPane.addAncestorListener(object : AncestorListenerAdapter() {
      override fun ancestorAdded(event: AncestorEvent) {
        rootPane.defaultButton = getButton(okAction)
      }
    })
  }

  /**
   * Returns a string representing the commit message made up
   * of the values inputted by the user.
   */
  fun getCommitMessage(): String {
    val (type, scope, subject, body, isBreakingChange, footers) = getCommitTokens()
    val sb = StringBuilder(256)

    // Type
    sb.append(type)

    // Scope
    if (scope.isNotEmpty()) {
      sb.append('(')
        .append(scope)
        .append(')')
    }

    // Breaking change mark "!"
    val processedFooters = footers.toMutableList()

    // Extract (and remove) the breaking change footer from all the footers
    val bcIndex = processedFooters.indexOfFirst { it.type.matches(breakingChangeRegex) }
    val breakingChange = if (bcIndex > -1) processedFooters.removeAt(bcIndex) else null

    if (isBreakingChange && breakingChange == null) {
      sb.append('!')
    }

    // Separator and subject
    sb.append(": ")
      .append(subject)

    // Body
    if (body.isNotEmpty()) {
      sb.append("\n\n")
        .append(body)
    }

    // Long breaking change description
    when {
      breakingChange != null ->
        sb.append("\n\n")
          .append("${breakingChange.type}: ")
          .append(breakingChange.value)
      processedFooters.isNotEmpty() ->
        sb.append("\n")
    }

    // All other footers
    processedFooters.forEach {
      sb.append("\n")
        .append("${it.type}: ")
        .append(it.value)
    }

    return "$sb"
  }

  private fun getCommitTokens(): CommitBuilderTokens {
    fun buildFooters() = buildList {
      val breakingChange = breakingChangeTextField.text.trim()

      // If screen reader support is on, the text field is always enabled
      // and thus we need to use its value regardless of the checkbox
      if ((breakingChangeCheckBox.isSelected || ScreenReader.isActive()) && breakingChange.isNotEmpty()) {
        add(CommitFooter("BREAKING CHANGE", breakingChange))
      }

      val footerType = footerTypeTextField.text.trim()
      val footerValue = footerValueTextField.text.trim()

      if (footerType.isNotEmpty() && footerValue.isNotEmpty()) {
        add(CommitFooter(footerType, footerValue))
      }
    }

    return CommitBuilderTokens(
        type = typeTextField.text.trim(),
        scope = scopeTextField.text.trim(),
        subject = subjectTextField.text.trim(),
        body = bodyTextField.text.trim(),
        isBreakingChange = breakingChangeCheckBox.isSelected,
        footers = buildFooters(),
    )
  }

  override fun getDimensionServiceKey(): String =
    "#com.github.lppedd.cc.commitbuilder.CommitBuilderDialog"

  override fun doOKAction() {
    saveFieldsValues()
    super.doOKAction()
  }

  override fun doCancelAction() {
    saveFieldsValues()
    super.doCancelAction()
  }

  override fun getPreferredFocusedComponent(): JComponent =
    typeTextField

  override fun validateAll(): List<ValidationInfo> =
    doValidateAll()

  override fun doValidateAll(): List<ValidationInfo> {
    val errors = mutableListOf<ValidationInfo?>()
    errors += typeValidator()
    errors += footerTypeValidator()
    errors += footerValueValidator()
    return errors.filterNotNull()
  }

  override fun createContentPaneBorder(): Border =
    JBUI.Borders.empty()

  override fun createTitlePane(): JComponent? {
    val properties = PropertiesComponent.getInstance()
    val doShow = properties.getBoolean(PROPERTY_HOWTO_SHOW, true)
    val completionShortcutText =
      KeymapUtil.getFirstKeyboardShortcutText(IdeActions.ACTION_CODE_COMPLETION)
        .ifEmpty { KeymapUtil.getFirstKeyboardShortcutText(IdeActions.ACTION_SMART_TYPE_COMPLETION) }

    // If there is no shortcut to invoke completion the banner should be shown
    // with an error message, regardless of a previous dismissal
    return if (doShow || completionShortcutText.isEmpty()) {
      // If there was no shortcut configured for completion and now there is one,
      // let's display the howto banner again to remind it to the user
      properties.setValue(PROPERTY_HOWTO_SHOW, true)
      HowToBanner(completionShortcutText)
    } else {
      null
    }
  }

  override fun createCenterPanel(): JComponent {
    val tokensPanel = JPanel(GridBagLayout())
    tokensPanel.accessibleContext.accessibleName = CCBundle["cc.commitbuilder.dialog.a11y.commitTokens"]

    val gc = GridBag()
      .setDefaultInsets(0, JBUI.insets(0, 2, 6, 6))
      .setDefaultInsets(1, JBUI.insetsBottom(6))
      .setDefaultAnchor(0, GridBagConstraints.LINE_START)
      .setDefaultAnchor(1, GridBagConstraints.LINE_END)
      .setDefaultWeightX(1, 1.0)
      .setDefaultFill(GridBagConstraints.HORIZONTAL)

    // Type
    val typeLabel = JBLabel(CCBundle["cc.commitbuilder.dialog.type"])
    typeLabel.labelFor = typeTextField
    tokensPanel.add(typeLabel, gc.nextLine().next())
    tokensPanel.add(typeTextField, gc.next())

    // Scope
    val scopeLabel = JBLabel(CCBundle["cc.commitbuilder.dialog.scope"])
    scopeLabel.labelFor = scopeTextField
    tokensPanel.add(scopeLabel, gc.nextLine().next())
    tokensPanel.add(scopeTextField, gc.next())

    // Subject
    val subjectLabel = JBLabel(CCBundle["cc.commitbuilder.dialog.subject"])
    subjectLabel.labelFor = subjectTextField
    tokensPanel.add(subjectLabel, gc.nextLine().next())
    tokensPanel.add(subjectTextField, gc.next())

    val allSoftWrapsShown = EditorSettingsExternalizable.getInstance().isAllSoftWrapsShown
    val softWrapsType = if (allSoftWrapsShown) {
      CCBundle["cc.commitbuilder.dialog.softWraps.all"]
    } else {
      CCBundle["cc.commitbuilder.dialog.softWraps.currentLine"]
    }
    val softWrapsComment = CCBundle["cc.commitbuilder.dialog.softWraps", softWrapsType]

    // Body
    val bodyTextFieldWithComment =
      UI.PanelFactory.panel(bodyTextField)
        .withComment(softWrapsComment)
        .resizeY(true)
        .createPanel()

    val bodyLabel = JBLabel(CCBundle["cc.commitbuilder.dialog.body"])
    bodyLabel.labelFor = bodyTextField
    tokensPanel.add(
        bodyLabel,
        gc.nextLine()
          .next()
          .anchor(GridBagConstraints.PAGE_START)
          .insets(5, 2, 0, 0),
    )
    tokensPanel.add(bodyTextFieldWithComment, gc.next().insets(JBUI.emptyInsets()))

    // Breaking change checkbox
    tokensPanel.add(
        breakingChangeCheckBox,
        gc.nextLine()
          .next()
          .coverLine()
          .insets(0, 0, 5, 0),
    )

    // Breaking change text area
    val breakingChangeTextFieldWithComment =
      UI.PanelFactory.panel(breakingChangeTextField)
        .withComment(softWrapsComment)
        .resizeY(true)
        .createPanel()

    val breakingChangePanel = JPanel(BorderLayout(0, 5.scaled))
    breakingChangePanel.add(breakingChangeTextFieldWithComment, BorderLayout.CENTER)

    tokensPanel.add(
        breakingChangePanel,
        gc.nextLine()
          .next()
          .coverLine()
          .insets(JBUI.emptyInsets()),
    )

    // Other footer
    tokensPanel.add(
        buildOtherFooterPanel(),
        gc.nextLine()
          .next()
          .coverLine()
          .insets(JBUI.emptyInsets()),
    )

    // Filler
    tokensPanel.add(
        Box.createVerticalGlue(),
        gc.nextLine()
          .coverLine()
          .weighty(1.0)
          .insets(JBUI.emptyInsets()),
    )

    val insets = JBUI.insets(UIUtil.PANEL_REGULAR_INSETS)
    insets.top += 5.scaled
    insets.bottom += 5.scaled
    tokensPanel.border = JBEmptyBorder(insets)

    return JBScrollPane(tokensPanel).also {
      it.border = JBUI.Borders.empty()
      it.viewport.addChangeListener { _ ->
        if (it.verticalScrollBar.isShowing || it.horizontalScrollBar.isShowing) {
          tokensPanel.revalidate()
        }
      }
    }
  }

  private fun buildOtherFooterPanel(): JPanel {
    val fieldsPanel = JPanel(GridLayout(1, 2, 3.scaled, 0)).also {
      it.add(footerTypeTextField)
      it.add(footerValueTextField)
    }

    val otherFooterPanel = JPanel(GridBagLayout())
    otherFooterPanel.accessibleContext.accessibleName = CCBundle["cc.commitbuilder.dialog.a11y.otherFooter"]

    val ogc = GridBag()
      .setDefaultInsets(0, JBUI.insetsRight(3))
      .setDefaultFill(GridBagConstraints.HORIZONTAL)
      .setDefaultWeightX(1.0)

    otherFooterPanel.add(
        SeparatorFactory.createSeparator(
            CCBundle["cc.commitbuilder.dialog.otherFooter"],
            footerTypeTextField,
        ),
        ogc.nextLine().insetLeft(2),
    )

    otherFooterPanel.add(fieldsPanel, ogc.nextLine())
    return otherFooterPanel
  }

  override fun createSouthPanel(): JComponent =
    super.createSouthPanel().also {
      it.border = CompoundBorder(
          CustomLineBorder(JBColor.border(), 1, 0, 0, 0),
          JBEmptyBorder(UIUtil.PANEL_REGULAR_INSETS),
      )
    }

  override fun createButtonsPanel(buttons: List<JButton>): JPanel =
    super.createButtonsPanel(buttons).also {
      // Since it seems the left actions panel border is overridden by platform code,
      // we need to set it on the right actions panel.
      // However here we have no way to know to which panel we're setting the border,
      // thus we have to check if a button represents the 'OK' action, which is
      // on the right actions panel
      if (buttons.any { b -> b.action === myOKAction }) {
        val ltr = it.componentOrientation.isLeftToRight
        it.border = if (ltr) JBUI.Borders.emptyLeft(20) else JBUI.Borders.emptyRight(20)
      }
    }

  override fun createDoNotAskCheckbox(): JComponent? =
    super.createDoNotAskCheckbox()?.let {
      val description = CCBundle["cc.commitbuilder.dialog.a11y.rememberValues"]
      it.accessibleContext.accessibleDescription = description
      UI.PanelFactory.panel(it)
        .withTooltip(description)
        .createPanel()
    }

  override fun getOKAction(): Action =
    super.getOKAction().also {
      it.setName(CCBundle["cc.commitbuilder.dialog.build"])
    }

  override fun isToBeShown(): Boolean =
    !commitMessageService.shouldRemember

  override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
    commitMessageService.shouldRemember = !toBeShown
  }

  override fun canBeHidden(): Boolean =
    true

  override fun shouldSaveOptionsOnCancel(): Boolean =
    true

  override fun getDoNotShowMessage(): String =
    CCBundle["cc.commitbuilder.dialog.rememberValues"]

  private fun initAccessibility() {
    breakingChangeTextField.accessibleContext.let {
      it.accessibleName = CCBundle["cc.commitbuilder.dialog.breakingChange.description"]

      val ars = it.accessibleRelationSet
      ars.add(AccessibleRelation(AccessibleRelation.LABELED_BY, breakingChangeCheckBox))
      ars.add(AccessibleRelation(AccessibleRelation.CONTROLLED_BY, breakingChangeCheckBox))
    }

    breakingChangeCheckBox.accessibleContext.let {
      val ars = it.accessibleRelationSet
      ars.add(AccessibleRelation(AccessibleRelation.LABEL_FOR, breakingChangeTextField))
      ars.add(AccessibleRelation(AccessibleRelation.CONTROLLER_FOR, breakingChangeTextField))
    }

    footerTypeTextField.accessibleContext.let {
      it.accessibleName = CCBundle["cc.commitbuilder.dialog.footerType"]
    }

    footerValueTextField.accessibleContext.let {
      it.accessibleName = CCBundle["cc.commitbuilder.dialog.footerValue"]
    }
  }

  private fun installValidators() {
    // Type
    ComponentValidator(disposable)
      .withValidator(::typeValidator)
      .withFocusValidator(::typeValidator)
      .withOutlineProvider { typeTextField }
      .installOn(typeTextField)
      .let {
        typeTextField.addDocumentListener(object : DocumentListener {
          override fun documentChanged(event: DocumentEvent) {
            it.revalidate()
          }
        })
      }

    // Footer type
    ComponentValidator(disposable)
      .withValidator(::footerTypeValidator)
      .withOutlineProvider { footerTypeTextField }
      .installOn(footerTypeTextField)
      .let {
        footerTypeTextField.addDocumentListener(object : DocumentListener {
          override fun documentChanged(event: DocumentEvent) {
            it.revalidate()
          }
        })
      }

    // Footer value
    ComponentValidator(disposable)
      .withValidator(::footerValueValidator)
      .withOutlineProvider { footerValueTextField }
      .installOn(footerValueTextField)
      .let {
        footerValueTextField.addDocumentListener(object : DocumentListener {
          override fun documentChanged(event: DocumentEvent) {
            it.revalidate()
          }
        })
      }
  }

  private fun typeValidator(): ValidationInfo? =
    if (typeTextField.text.isBlank()) {
      val message = CCBundle["cc.commitbuilder.dialog.validation.type.empty"]
      typeTextField.accessibleContext.accessibleDescription = message
      ValidationInfo(message, typeTextField)
    } else {
      typeTextField.accessibleContext.accessibleDescription = null
      null
    }

  private fun footerTypeValidator(): ValidationInfo? {
    val text = footerTypeTextField.text.trim()

    if (text.matches(breakingChangeRegex)) {
      val message = CCBundle["cc.commitbuilder.dialog.validation.footerType.useBreakingChange"]
      footerTypeTextField.accessibleContext.accessibleDescription = message
      return ValidationInfo(message, footerTypeTextField)
    }

    if (text.isEmpty() && footerValueTextField.text.isNotBlank()) {
      val message = CCBundle["cc.commitbuilder.dialog.validation.footerType.empty"]
      footerTypeTextField.accessibleContext.accessibleDescription = message
      return ValidationInfo(message, footerTypeTextField)
    }

    footerTypeTextField.accessibleContext.accessibleDescription = null
    return null
  }

  private fun footerValueValidator(): ValidationInfo? {
    if (footerValueTextField.text.isBlank() && footerTypeTextField.text.isNotBlank()) {
      val message = CCBundle["cc.commitbuilder.dialog.validation.footerValue.empty"]
      footerValueTextField.accessibleContext.accessibleDescription = message
      return ValidationInfo(message, footerValueTextField)
    }

    footerValueTextField.accessibleContext.accessibleDescription = null
    return null
  }

  /**
   * We save what the user inputted on each field in case he needs
   * to close the dialog for whatever reason and reopen it later.
   *
   * Values are cleared as soon as the commit succeed.
   */
  private fun saveFieldsValues() {
    if (!myCheckBoxDoNotShowDialog.isSelected) {
      return
    }

    commitMessageService.clear()
    commitMessageService.type = typeTextField.text
    commitMessageService.scope = scopeTextField.text
    commitMessageService.subject = subjectTextField.text
    commitMessageService.body = bodyTextField.text
    commitMessageService.isBreakingChange = breakingChangeCheckBox.isSelected

    if (breakingChangeCheckBox.isSelected) {
      val breakingChangeDescription = breakingChangeTextField.text.trim()

      if (breakingChangeDescription.isNotEmpty()) {
        commitMessageService.addFooter(CommitFooter("BREAKING CHANGE", breakingChangeDescription))
      }
    }

    commitMessageService.addFooter(
        CommitFooter(
            footerTypeTextField.text.trim(),
            footerValueTextField.text.trim()
        )
    )
  }

  private fun restoreFieldsValues() {
    if (!commitMessageService.shouldRemember) {
      return
    }

    typeTextField.text = commitMessageService.type
    scopeTextField.text = commitMessageService.scope
    subjectTextField.text = commitMessageService.subject
    bodyTextField.text = commitMessageService.body
    breakingChangeCheckBox.isSelected = commitMessageService.isBreakingChange

    // Restore the breaking change footer value
    commitMessageService.getFooter("BREAKING CHANGE")?.let {
      breakingChangeTextField.text = it.value
    }

    // Restore the "other footer"
    val otherFooter = commitMessageService.getFooters()
      .firstOrNull { (type) -> !type.matches(breakingChangeRegex) }

    if (otherFooter != null) {
      footerTypeTextField.text = otherFooter.type
      footerValueTextField.text = otherFooter.value
    }
  }

  data class CommitBuilderTokens(
      val type: String,
      val scope: String,
      val subject: String,
      val body: String,
      val isBreakingChange: Boolean,
      val footers: List<CommitFooter>,
  )

  private inner class CommitTypeCompletionProvider : CommitTokenCompletionProvider() {
    override fun fillVariants(prefix: String, resultSet: CompletionResultSet) {
      val context = TypeCommitContext(prefix)
      val provider = TypeCompletionProvider(project, context)
      provider.complete(TextFieldResultSet(resultSet))
    }
  }

  private inner class CommitScopeCompletionProvider : CommitTokenCompletionProvider() {
    override fun fillVariants(prefix: String, resultSet: CompletionResultSet) {
      val context = ScopeCommitContext(typeTextField.text, prefix)
      val provider = ScopeCompletionProvider(project, context)
      provider.complete(TextFieldResultSet(resultSet))
    }
  }

  private inner class CommitSubjectCompletionProvider : CommitTokenCompletionProvider() {
    override fun fillVariants(prefix: String, resultSet: CompletionResultSet) {
      val context = SubjectCommitContext(typeTextField.text, scopeTextField.text, prefix)
      val provider = SubjectCompletionProvider(project, context)
      provider.complete(TextFieldResultSet(resultSet))
    }
  }

  private inner class CommitBodyCompletionProvider : CommitTokenCompletionProvider() {
    override fun fillVariants(prefix: String, resultSet: CompletionResultSet) {
      val commitTokens = CommitTokens(
          type = ValidToken(typeTextField.text, TextRange.EMPTY_RANGE),
          scope = ValidToken(scopeTextField.text, TextRange.EMPTY_RANGE),
          subject = ValidToken(subjectTextField.text, TextRange.EMPTY_RANGE),
      )
      val provider = BodyCompletionProvider(project, FooterTypeContext(prefix), commitTokens)
      provider.complete(TextFieldResultSet(resultSet))
    }
  }

  private inner class CommitFooterTypeCompletionProvider : CommitTokenCompletionProvider() {
    override fun fillVariants(prefix: String, resultSet: CompletionResultSet) {
      val rs = TextFieldResultSet(resultSet).withPrefixMatcher(prefix)

      FOOTER_TYPE_EP.getExtensions(project).asSequence()
        .sortedBy(configService::getProviderOrder)
        .flatMap { provider ->
          val wrapper = FooterTypeProviderWrapper(project, provider)
          provider.getCommitFooterTypes()
            .asSequence()
            // We don't have to display a "BREAKING CHANGE" footer type
            // as it is already covered with its own text field
            .filterNot { it.value.matches(breakingChangeRegex) }
            .take(Provider.MaxItems)
            .map { wrapper to it }
        }
        .mapIndexed { index, (provider, commitFooterType) ->
          CommitFooterTypeLookupElement(
              index,
              provider,
              CommitFooterTypePsiElement(project, commitFooterType),
          )
        }
        .distinctBy(CommitFooterTypeLookupElement::getLookupString)
        .forEach(rs::addElement)

      rs.stopHere()
    }
  }

  private inner class CommitFooterValueCompletionProvider(
      val footerTypeProducer: () -> String,
  ) : CommitTokenCompletionProvider() {
    override fun fillVariants(prefix: String, resultSet: CompletionResultSet) {
      val rs = TextFieldResultSet(resultSet).withPrefixMatcher(prefix)

      FOOTER_VALUE_EP.getExtensions(project).asSequence()
        .sortedBy(configService::getProviderOrder)
        .flatMap { provider ->
          val wrapper = FooterValueProviderWrapper(project, provider)
          provider.getCommitFooterValues(
              footerTypeProducer().trim(),
              typeTextField.text.trim(),
              scopeTextField.text.trim(),
              subjectTextField.text.trim(),
          )
            .asSequence()
            .take(Provider.MaxItems)
            .map { wrapper to it }
        }
        .mapIndexed { index, (provider, commitFooterValue) ->
          CommitFooterValueLookupElement(
              index,
              provider,
              CommitFooterValuePsiElement(project, commitFooterValue),
          )
        }
        .distinctBy(CommitFooterValueLookupElement::getLookupString)
        .forEach(rs::addElement)

      rs.stopHere()
    }
  }
}
