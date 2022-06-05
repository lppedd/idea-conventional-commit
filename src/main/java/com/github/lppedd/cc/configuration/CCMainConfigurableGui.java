package com.github.lppedd.cc.configuration;

import static com.intellij.uiDesigner.core.GridConstraints.*;
import static org.apache.commons.validator.routines.UrlValidator.NO_FRAGMENTS;

import java.awt.*;
import java.util.Map;

import javax.swing.*;

import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.CCBundle;
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType;
import com.github.lppedd.cc.configuration.CCDefaultTokensService.JsonCommitType;
import com.github.lppedd.cc.configuration.component.CoAuthorsFilePickerPanel;
import com.github.lppedd.cc.configuration.component.DefaultTokensFileExportPanel;
import com.github.lppedd.cc.configuration.component.DefaultTokensFilePickerPanel;
import com.github.lppedd.cc.configuration.component.DefaultTokensPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.UIUtil.FontSize;

/**
 * @author Edoardo Luppi
 */
public class CCMainConfigurableGui {
  private static final int INDENT = 10;

  private JPanel rootPanel;
  private JPanel infoPanel;
  private JBLabel info;

  private JPanel completionTypePanel;
  private final ButtonGroup group = new ButtonGroup();
  private final JBRadioButton isPopup = new JBRadioButton(CCBundle.get("cc.config.popup"));
  private final JBRadioButton isTemplate = new JBRadioButton(CCBundle.get("cc.config.template"));

  private JPanel completionOptionsPanel;
  private final JBCheckBox isEnableLanguageSupport =
      new JBCheckBox(CCBundle.get("cc.config.enableLanguageSupport"));
  private final JBCheckBox isPrioritizeRecentlyUsed =
      new JBCheckBox(CCBundle.get("cc.config.prioritizeRecentlyUsed"));
  private final JBCheckBox isAutoInsertSpaceAfterColon =
      new JBCheckBox(CCBundle.get("cc.config.autoInsertSpaceAfterColon"));

  private JPanel coAuthorsPanel;
  private JPanel defaultsPanel;
  private DefaultTokensFilePickerPanel defaultTokensFilePickerPanel;
  private final DefaultTokensPanel defaultTokensPanel = new DefaultTokensPanel();
  private CoAuthorsFilePickerPanel coAuthorsFilePickerPanel;

  public CCMainConfigurableGui(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    this();
    finishUpComponents(project, disposable);
  }

  private CCMainConfigurableGui() {}

  @NotNull
  public JPanel getRootPanel() {
    return rootPanel;
  }

  @NotNull
  public CompletionType getCompletionType() {
    if (isPopup.isSelected()) {
      return CompletionType.POPUP;
    }

    if (isTemplate.isSelected()) {
      return CompletionType.TEMPLATE;
    }

    throw new IllegalStateException("A radio button should be selected");
  }

  public boolean isEnableLanguageSupport() {
    return isEnableLanguageSupport.isSelected();
  }

  public boolean isPrioritizeRecentlyUsed() {
    return isPrioritizeRecentlyUsed.isSelected();
  }

  public boolean isAutoInsertSpaceAfterColon() {
    return isAutoInsertSpaceAfterColon.isSelected();
  }

  @Nullable
  public String getCustomCoAuthorsFilePath() {
    return coAuthorsFilePickerPanel.getCustomFilePath();
  }

  @Nullable
  public String getCustomTokensFilePath() {
    return defaultTokensFilePickerPanel.getCustomFilePath();
  }

  public void setCompletionType(@NotNull final CompletionType completionType) {
    switch (completionType) {
      case POPUP:
        isPopup.setSelected(true);
        break;
      case TEMPLATE:
        isTemplate.setSelected(true);
        break;
      default:
        break;
    }
  }

  public void setEnableLanguageSupport(final boolean isEnableLanguageSupport) {
    this.isEnableLanguageSupport.setSelected(isEnableLanguageSupport);
  }

  public void setPrioritizeRecentlyUsed(final boolean isPrioritizeRecentlyUsed) {
    this.isPrioritizeRecentlyUsed.setSelected(isPrioritizeRecentlyUsed);
  }

  public void setAutoInsertSpaceAfterColon(final boolean isAutoInsertSpaceAfterColon) {
    this.isAutoInsertSpaceAfterColon.setSelected(isAutoInsertSpaceAfterColon);
  }

  public void setCustomCoAuthorsFilePath(@Nullable final String path) {
    coAuthorsFilePickerPanel.setCustomFilePath(path);
  }

  public void setCustomTokensFilePath(@Nullable final String path) {
    defaultTokensFilePickerPanel.setCustomFilePath(path);
  }

  public void setTokens(@NotNull final Map<String, JsonCommitType> tokens) {
    defaultTokensPanel.setTokens(tokens);
  }

  public boolean isValid() {
    return coAuthorsFilePickerPanel.isComponentValid() &&
           defaultTokensFilePickerPanel.isComponentValid();
  }

  public void revalidate() {
    coAuthorsFilePickerPanel.revalidateComponent();
    defaultTokensFilePickerPanel.revalidateComponent();
  }

  @SuppressWarnings("ConstantExpression")
  private void finishUpComponents(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    completionTypePanel.setLayout(
        new GridLayoutManager(2, 1, JBUI.insetsLeft(INDENT), 0, JBUIScale.scale(5))
    );

    final var gcCtp = new GridConstraints();
    gcCtp.setFill(FILL_HORIZONTAL);
    completionTypePanel.add(isPopup, gcCtp);

    gcCtp.setRow(1);
    completionTypePanel.add(isTemplate, gcCtp);

    group.add(isPopup);
    group.add(isTemplate);

    final var translatorLabel = buildTranslatorLabel();

    if (translatorLabel != null) {
      infoPanel.add(translatorLabel, BorderLayout.LINE_END);
    }

    JBUI.Borders.emptyBottom(5).wrap(infoPanel);
    info.setText(CCBundle.get("cc.config.info"));

    completionOptionsPanel.setLayout(new BoxLayout(completionOptionsPanel, BoxLayout.Y_AXIS));
    completionOptionsPanel.setBorder(JBUI.Borders.emptyTop(10));
    completionOptionsPanel.add(isEnableLanguageSupport);
    completionOptionsPanel.add(Box.createVerticalStrut(5));
    completionOptionsPanel.add(isPrioritizeRecentlyUsed);
    completionOptionsPanel.add(Box.createVerticalStrut(5));
    completionOptionsPanel.add(isAutoInsertSpaceAfterColon);

    coAuthorsPanel.setLayout(new BorderLayout());
    coAuthorsPanel.setBorder(
        IdeBorderFactory.createTitledBorder(
            CCBundle.get("cc.config.coAuthors.title"),
            false,
            JBUI.insetsTop(10)
        )
    );

    coAuthorsPanel.add(Box.createHorizontalStrut(JBUIScale.scale(INDENT)), BorderLayout.LINE_START);
    coAuthorsFilePickerPanel = new CoAuthorsFilePickerPanel(project, disposable);
    coAuthorsPanel.add(coAuthorsFilePickerPanel, BorderLayout.CENTER);

    defaultsPanel.setLayout(new GridLayoutManager(3, 1, JBUI.insetsLeft(INDENT), 0, 0));
    defaultsPanel.setBorder(
        IdeBorderFactory.createTitledBorder(
            CCBundle.get("cc.config.defaults"),
            false,
            JBUI.insetsTop(3)
        )
    );

    final var gc = new GridConstraints();
    gc.setFill(FILL_BOTH);
    gc.setHSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(JBUI.Borders.empty(0, 1, 16, 0).wrap(new DefaultTokensFileExportPanel()), gc);

    gc.setRow(1);
    defaultTokensFilePickerPanel = new DefaultTokensFilePickerPanel(project, disposable);
    defaultsPanel.add(defaultTokensFilePickerPanel, gc);
    defaultTokensFilePickerPanel.revalidateComponent();

    gc.setRow(2);
    gc.setVSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(JBUI.Borders.emptyTop(7).wrap(defaultTokensPanel), gc);
  }

  @Nullable
  private HyperlinkLabel buildTranslatorLabel() {
    final var name = CCBundle.getWithDefault("cc.translation.translator.name", "");

    if (name.isEmpty()) {
      return null;
    }

    final var label = new HyperlinkLabel();
    label.setForeground(UIUtil.getContextHelpForeground());
    label.setFontSize(FontSize.SMALL);

    final var url = CCBundle.getWithDefault("cc.translation.translator.url", "");

    if (url.isEmpty()) {
      label.setHyperlinkText(CCBundle.get("cc.translation.text") + " " + name, "", "");
    } else {
      if (new UrlValidator(new String[] {"http", "https"}, NO_FRAGMENTS).isValid(url)) {
        label.setHyperlinkText(CCBundle.get("cc.translation.text") + " ", name, "");
        label.setHyperlinkTarget(url);
      } else {
        label.setHyperlinkText(CCBundle.get("cc.translation.text") + " " + name, "", "");
      }
    }

    // Keep this here or it will be overwritten
    label.setIcon(null);
    return label;
  }
}
